package com.electricalwarehouse.service;

import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Loads referenced entities by id or fails with a 404 the client understands. */
@Component
@RequiredArgsConstructor
public class Lookup {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public Warehouse warehouse(Long id) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Kho", id));
        if (!w.isActive()) {
            throw new BusinessException("Kho " + w.getCode() + " đã ngừng hoạt động");
        }
        return w;
    }

    public Product product(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Sản phẩm", id));
    }

    /** Null id means "no specific location". The location must belong to the given warehouse. */
    public StorageLocation location(Long id, Long warehouseId) {
        if (id == null) {
            return null;
        }
        StorageLocation loc = locationRepository.findById(id).orElseThrow(() -> new NotFoundException("Vị trí", id));
        if (!loc.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Vị trí " + loc.getCode() + " không thuộc kho đã chọn");
        }
        return loc;
    }

    public Supplier supplier(Long id) {
        return id == null ? null : supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Nhà cung cấp", id));
    }

    public Customer customer(Long id) {
        return id == null ? null : customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Khách hàng", id));
    }

    public User user(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Người dùng", id));
    }
}
