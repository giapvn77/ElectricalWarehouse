package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.config.AppProperties;
import com.electricalwarehouse.dto.request.CategoryRequest;
import com.electricalwarehouse.dto.request.ProductRequest;
import com.electricalwarehouse.dto.request.UnitRequest;
import com.electricalwarehouse.dto.response.CategoryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.ProductResponse;
import com.electricalwarehouse.dto.response.UnitResponse;
import com.electricalwarehouse.entity.Product;
import com.electricalwarehouse.entity.ProductCategory;
import com.electricalwarehouse.entity.ProductImage;
import com.electricalwarehouse.entity.Unit;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.ProductCategoryRepository;
import com.electricalwarehouse.repository.ProductImageRepository;
import com.electricalwarehouse.repository.ProductRepository;
import com.electricalwarehouse.repository.UnitRepository;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductImageRepository imageRepository;
    private final AuditService auditService;
    private final AppProperties props;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ProductResponse> search(String keyword, Long categoryId, Pageable pageable) {
        return PagedResult.of(productRepository.search(InventoryServiceImpl.blankToNull(keyword), categoryId, pageable),
                mapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return withImages(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findByCode(String code) {
        String c = code.trim();
        return productRepository.findByBarcode(c).or(() -> productRepository.findBySku(c))
                .map(this::withImages)
                .orElseThrow(() -> new NotFoundException("Sản phẩm", code));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new BusinessException("Mã SKU đã tồn tại");
        }
        Product product = new Product();
        apply(product, req);
        productRepository.save(product);
        auditService.log("CREATE", "PRODUCT", product.getId(), product.getSku());
        return mapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product product = find(id);
        if (!product.getSku().equals(req.sku()) && productRepository.existsBySku(req.sku())) {
            throw new BusinessException("Mã SKU đã tồn tại");
        }
        apply(product, req);
        auditService.log("UPDATE", "PRODUCT", id, product.getSku());
        return withImages(product);
    }

    private void apply(Product product, ProductRequest req) {
        mapper.updateProduct(req, product);
        if (!StringUtils.hasText(product.getBarcode())) {
            product.setBarcode(null);
        } else if (productRepository.existsByBarcodeAndIdNot(product.getBarcode(),
                product.getId() != null ? product.getId() : -1L)) {
            throw new BusinessException("Mã vạch đã được dùng cho sản phẩm khác");
        }
        product.setCategory(req.categoryId() == null ? null : categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Nhóm hàng", req.categoryId())));
        product.setUnit(req.unitId() == null ? null : unitRepository.findById(req.unitId())
                .orElseThrow(() -> new NotFoundException("Đơn vị tính", req.unitId())));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        find(id).setActive(false);
        auditService.log("DEACTIVATE", "PRODUCT", id, null);
    }

    @Override
    @Transactional
    public ProductResponse addImage(Long id, MultipartFile file) {
        Product product = find(id);
        String ext = IMAGE_TYPES.get(file.getContentType());
        if (ext == null) {
            throw new BusinessException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP");
        }
        // Server-generated file name: never trust the client's name or path.
        String fileName = "products/" + product.getId() + "/" + UUID.randomUUID() + ext;
        Path target = Path.of(props.uploadDir()).resolve(fileName).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Không lưu được ảnh", e);
        }
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl("/uploads/" + fileName);
        image.setPrimaryImage(imageRepository.findByProductId(id).isEmpty());
        imageRepository.save(image);
        return withImages(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll(Sort.by("code")).stream().map(mapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse saveCategory(Long id, CategoryRequest req) {
        ProductCategory category = id == null ? new ProductCategory()
                : categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Nhóm hàng", id));
        if (id == null && categoryRepository.existsByCode(req.code())) {
            throw new BusinessException("Mã nhóm hàng đã tồn tại");
        }
        mapper.updateCategory(req, category);
        if (req.parentId() != null && req.parentId().equals(id)) {
            throw new BusinessException("Nhóm hàng không thể là cha của chính nó");
        }
        category.setParent(req.parentId() == null ? null : categoryRepository.findById(req.parentId())
                .orElseThrow(() -> new NotFoundException("Nhóm hàng", req.parentId())));
        return mapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnits() {
        return unitRepository.findAll(Sort.by("code")).stream().map(mapper::toUnitResponse).toList();
    }

    @Override
    @Transactional
    public UnitResponse saveUnit(Long id, UnitRequest req) {
        Unit unit = id == null ? new Unit()
                : unitRepository.findById(id).orElseThrow(() -> new NotFoundException("Đơn vị tính", id));
        if (id == null && unitRepository.existsByCode(req.code())) {
            throw new BusinessException("Mã đơn vị tính đã tồn tại");
        }
        mapper.updateUnit(req, unit);
        return mapper.toUnitResponse(unitRepository.save(unit));
    }

    private ProductResponse withImages(Product product) {
        List<String> urls = imageRepository.findByProductId(product.getId()).stream()
                .sorted((a, b) -> Boolean.compare(b.isPrimaryImage(), a.isPrimaryImage()))
                .map(ProductImage::getUrl).toList();
        return mapper.toProductResponse(product, urls);
    }

    private Product find(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Sản phẩm", id));
    }
}
