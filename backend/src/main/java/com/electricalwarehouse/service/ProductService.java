package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.CategoryRequest;
import com.electricalwarehouse.dto.request.ProductRequest;
import com.electricalwarehouse.dto.request.UnitRequest;
import com.electricalwarehouse.dto.response.CategoryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.ProductResponse;
import com.electricalwarehouse.dto.response.UnitResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Products plus their categories, units and images. */
public interface ProductService {

    PagedResult<ProductResponse> search(String keyword, Long categoryId, Pageable pageable);

    ProductResponse get(Long id);

    ProductResponse findByCode(String skuOrBarcode);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    /** Soft delete: products referenced by documents must stay readable. */
    void deactivate(Long id);

    ProductResponse addImage(Long id, MultipartFile file);

    List<CategoryResponse> getCategories();

    CategoryResponse saveCategory(Long id, CategoryRequest request);

    List<UnitResponse> getUnits();

    UnitResponse saveUnit(Long id, UnitRequest request);
}
