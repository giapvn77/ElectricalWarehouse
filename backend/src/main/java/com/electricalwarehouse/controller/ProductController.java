package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.CategoryRequest;
import com.electricalwarehouse.dto.request.ProductRequest;
import com.electricalwarehouse.dto.request.UnitRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.CategoryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.ProductResponse;
import com.electricalwarehouse.dto.response.UnitResponse;
import com.electricalwarehouse.integration.BarcodeService;
import com.electricalwarehouse.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final BarcodeService barcodeService;

    @GetMapping("/products")
    public ApiResponse<PagedResult<ProductResponse>> search(@RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) Long categoryId,
                                                            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(productService.search(keyword, categoryId, pageable));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.get(id));
    }

    /** Find by barcode or SKU (used by the barcode scanner). */
    @GetMapping("/products/lookup")
    public ApiResponse<ProductResponse> lookup(@RequestParam String code) {
        return ApiResponse.ok(productService.findByCode(code));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ApiResponse.ok();
    }

    @PostMapping(value = "/products/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<ProductResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(productService.addImage(id, file));
    }

    @GetMapping(value = "/products/{id}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> barcode(@PathVariable Long id) {
        ProductResponse p = productService.get(id);
        String content = p.barcode() != null ? p.barcode() : p.sku();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(barcodeService.code128(content, 360, 100));
    }

    @GetMapping(value = "/products/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id) {
        ProductResponse p = productService.get(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(barcodeService.qrCode(p.sku(), 240));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(productService.getCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(productService.saveCategory(null, request));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(productService.saveCategory(id, request));
    }

    @GetMapping("/units")
    public ApiResponse<List<UnitResponse>> units() {
        return ApiResponse.ok(productService.getUnits());
    }

    @PostMapping("/units")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<UnitResponse> createUnit(@Valid @RequestBody UnitRequest request) {
        return ApiResponse.ok(productService.saveUnit(null, request));
    }

    @PutMapping("/units/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<UnitResponse> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitRequest request) {
        return ApiResponse.ok(productService.saveUnit(id, request));
    }
}
