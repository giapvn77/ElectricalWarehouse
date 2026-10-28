package com.electricalwarehouse.mapper;

import com.electricalwarehouse.dto.request.CategoryRequest;
import com.electricalwarehouse.dto.request.PartnerRequest;
import com.electricalwarehouse.dto.request.ProductRequest;
import com.electricalwarehouse.dto.request.StorageLocationRequest;
import com.electricalwarehouse.dto.request.UnitRequest;
import com.electricalwarehouse.dto.request.WarehouseRequest;
import com.electricalwarehouse.dto.response.*;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.RoleCode;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

/** Entity <-> DTO conversions. Call inside a transaction: lazy relations are read here. */
@Mapper
public interface DTOMapper {

    // ---------- System ----------
    @Mapping(target = "warehouseId", source = "user.warehouse.id")
    @Mapping(target = "warehouseName", source = "user.warehouse.name")
    UserResponse toUserResponse(User user, Set<RoleCode> roles, Set<String> permissions);

    PermissionResponse toPermissionResponse(Permission permission);

    default RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(),
                role.getPermissions().stream().map(Permission::getCode)
                        .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new)));
    }

    @Mapping(target = "username", source = "user.username")
    AuditLogResponse toAuditLogResponse(AuditLog log);

    NotificationResponse toNotificationResponse(Notification notification);

    // ---------- Catalog ----------
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryResponse toCategoryResponse(ProductCategory category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parent", ignore = true)
    void updateCategory(CategoryRequest request, @MappingTarget ProductCategory category);

    UnitResponse toUnitResponse(Unit unit);

    void updateUnit(UnitRequest request, @MappingTarget Unit unit);

    @Mapping(target = "categoryId", source = "product.category.id")
    @Mapping(target = "categoryName", source = "product.category.name")
    @Mapping(target = "unitId", source = "product.unit.id")
    @Mapping(target = "unitName", source = "product.unit.name")
    ProductResponse toProductResponse(Product product, List<String> imageUrls);

    default ProductResponse toProductResponse(Product product) {
        return toProductResponse(product, List.of());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "unit", ignore = true)
    void updateProduct(ProductRequest request, @MappingTarget Product product);

    PartnerResponse toPartnerResponse(Supplier supplier);

    PartnerResponse toPartnerResponse(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSupplier(PartnerRequest request, @MappingTarget Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomer(PartnerRequest request, @MappingTarget Customer customer);

    WarehouseResponse toWarehouseResponse(Warehouse warehouse);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWarehouse(WarehouseRequest request, @MappingTarget Warehouse warehouse);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    StorageLocationResponse toLocationResponse(StorageLocation location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLocation(StorageLocationRequest request, @MappingTarget StorageLocation location);

    // ---------- Stock in ----------
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    StockInResponse toStockInResponse(StockInReceipt receipt);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "serialManaged", source = "product.serialManaged")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationCode", source = "location.code")
    StockInDetailResponse toStockInDetailResponse(StockInReceiptDetail detail);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "locationCode", source = "location.code")
    SerialResponse toSerialResponse(ProductSerial serial);

    // ---------- Stock out ----------
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "pickedByName", source = "pickedBy.fullName")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    StockOutResponse toStockOutResponse(StockOutReceipt receipt);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "serialManaged", source = "product.serialManaged")
    @Mapping(target = "serialNumbers", source = "serialAllocations")
    StockOutDetailResponse toStockOutDetailResponse(StockOutReceiptDetail detail);

    default List<String> toSerialNumbers(List<StockOutSerialAllocation> allocations) {
        return allocations.stream().map(a -> a.getSerial().getSerialNumber()).toList();
    }

    // ---------- Inventory ----------
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitName", source = "product.unit.name")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationCode", source = "location.code")
    InventoryResponse toInventoryResponse(Inventory inventory);

    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "locationCode", source = "location.code")
    @Mapping(target = "performedByName", source = "performedBy.fullName")
    StockTransactionResponse toTransactionResponse(StockTransaction transaction);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "locationCode", source = "location.code")
    AdjustmentResponse toAdjustmentResponse(InventoryAdjustment adjustment);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    ThresholdResponse toThresholdResponse(InventoryThreshold threshold);

    // ---------- Transfer & defects ----------
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "receivedByName", source = "receivedBy.fullName")
    @Mapping(target = "fromWarehouseId", source = "fromWarehouse.id")
    @Mapping(target = "fromWarehouseName", source = "fromWarehouse.name")
    @Mapping(target = "toWarehouseId", source = "toWarehouse.id")
    @Mapping(target = "toWarehouseName", source = "toWarehouse.name")
    TransferResponse toTransferResponse(StockTransfer transfer);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "toLocationId", source = "toLocation.id")
    @Mapping(target = "toLocationCode", source = "toLocation.code")
    TransferDetailResponse toTransferDetailResponse(StockTransferDetail detail);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "serialNumber", source = "serial.serialNumber")
    @Mapping(target = "sourceWarehouseName", source = "sourceWarehouse.name")
    @Mapping(target = "defectiveWarehouseName", source = "defectiveWarehouse.name")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "reportedByName", source = "reportedBy.fullName")
    DefectiveGoodResponse toDefectiveGoodResponse(DefectiveGood defect);
}
