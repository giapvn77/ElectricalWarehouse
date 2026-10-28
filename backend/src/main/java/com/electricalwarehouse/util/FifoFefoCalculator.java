package com.electricalwarehouse.util;

import com.electricalwarehouse.entity.Inventory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Chooses which inventory rows (location + lot) to take goods from.
 * FEFO (first expired, first out) for expiry-managed products, FIFO (first received) otherwise.
 */
@Component
public class FifoFefoCalculator {

    /** Which part of a row's stock may be taken. */
    public enum Pool {
        /** quantity - reserved: for new reservations and unreserved issues. */
        AVAILABLE(Inventory::getAvailableQuantity),
        /** reserved only: for issuing goods reserved earlier. */
        RESERVED(Inventory::getReservedQuantity);

        private final ToIntFunction<Inventory> amount;

        Pool(ToIntFunction<Inventory> amount) {
            this.amount = amount;
        }
    }

    public record Allocation(Inventory inventory, int quantity) {
    }

    public record Plan(List<Allocation> allocations, int requested, int allocated) {
        public boolean isComplete() {
            return allocated >= requested;
        }

        public int shortage() {
            return Math.max(0, requested - allocated);
        }
    }

    public List<Inventory> sortForPicking(List<Inventory> rows, boolean expiryManaged) {
        Comparator<Inventory> byReceived = Comparator.comparing(Inventory::getReceivedAt)
                .thenComparing(Inventory::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Inventory> order = expiryManaged
                ? Comparator.comparing(Inventory::getExpiryDate, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(byReceived)
                : byReceived;
        return rows.stream().sorted(order).toList();
    }

    /** Greedy allocation in picking order. Never allocates more than each row holds in the pool. */
    public Plan allocate(List<Inventory> rows, int quantity, boolean expiryManaged, Pool pool) {
        List<Allocation> allocations = new ArrayList<>();
        int remaining = quantity;
        for (Inventory row : sortForPicking(rows, expiryManaged)) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(remaining, pool.amount.applyAsInt(row));
            if (take > 0) {
                allocations.add(new Allocation(row, take));
                remaining -= take;
            }
        }
        return new Plan(allocations, quantity, quantity - remaining);
    }
}
