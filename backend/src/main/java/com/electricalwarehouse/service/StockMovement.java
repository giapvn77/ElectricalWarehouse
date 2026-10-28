package com.electricalwarehouse.service;

import com.electricalwarehouse.entity.Product;
import com.electricalwarehouse.entity.StorageLocation;
import com.electricalwarehouse.entity.Warehouse;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One stock change request. quantity is always positive; the method called (increase/decrease)
 * decides the sign. location and lotNumber may be null.
 */
public record StockMovement(Warehouse warehouse, Product product, StorageLocation location, String lotNumber,
                            LocalDate expiryDate, int quantity, TransactionType type, ReferenceType referenceType,
                            Long referenceId, String referenceCode, BigDecimal unitCost, String note) {

    public static StockMovement of(Warehouse warehouse, Product product, int quantity, TransactionType type,
                                   ReferenceType refType, Long refId, String refCode) {
        return new StockMovement(warehouse, product, null, null, null, quantity, type, refType, refId, refCode,
                null, null);
    }

    public StockMovement at(StorageLocation location, String lotNumber, LocalDate expiryDate) {
        return new StockMovement(warehouse, product, location, lotNumber, expiryDate, quantity, type, referenceType,
                referenceId, referenceCode, unitCost, note);
    }

    public StockMovement withQuantity(int newQuantity) {
        return new StockMovement(warehouse, product, location, lotNumber, expiryDate, newQuantity, type,
                referenceType, referenceId, referenceCode, unitCost, note);
    }

    public StockMovement withUnitCost(BigDecimal cost) {
        return new StockMovement(warehouse, product, location, lotNumber, expiryDate, quantity, type, referenceType,
                referenceId, referenceCode, cost, note);
    }
}
