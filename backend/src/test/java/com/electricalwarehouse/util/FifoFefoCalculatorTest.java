package com.electricalwarehouse.util;

import com.electricalwarehouse.entity.Inventory;
import com.electricalwarehouse.util.FifoFefoCalculator.Plan;
import com.electricalwarehouse.util.FifoFefoCalculator.Pool;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FifoFefoCalculatorTest {

    private final FifoFefoCalculator calculator = new FifoFefoCalculator();

    private static Inventory row(long id, int qty, int reserved, LocalDateTime received, LocalDate expiry) {
        Inventory i = new Inventory();
        i.setId(id);
        i.setQuantity(qty);
        i.setReservedQuantity(reserved);
        i.setReceivedAt(received);
        i.setExpiryDate(expiry);
        return i;
    }

    @Test
    void fifoTakesOldestReceiptFirst() {
        LocalDateTime now = LocalDateTime.now();
        Inventory newer = row(1, 10, 0, now, null);
        Inventory older = row(2, 5, 0, now.minusDays(3), null);

        Plan plan = calculator.allocate(List.of(newer, older), 8, false, Pool.AVAILABLE);

        assertThat(plan.isComplete()).isTrue();
        assertThat(plan.allocations()).extracting(a -> a.inventory().getId()).containsExactly(2L, 1L);
        assertThat(plan.allocations()).extracting(FifoFefoCalculator.Allocation::quantity).containsExactly(5, 3);
    }

    @Test
    void fefoTakesEarliestExpiryFirstEvenIfReceivedLater() {
        LocalDateTime now = LocalDateTime.now();
        Inventory oldLongExpiry = row(1, 10, 0, now.minusDays(10), LocalDate.now().plusYears(1));
        Inventory newShortExpiry = row(2, 10, 0, now, LocalDate.now().plusDays(5));

        Plan plan = calculator.allocate(List.of(oldLongExpiry, newShortExpiry), 4, true, Pool.AVAILABLE);

        assertThat(plan.allocations()).singleElement().satisfies(a -> {
            assertThat(a.inventory().getId()).isEqualTo(2L);
            assertThat(a.quantity()).isEqualTo(4);
        });
    }

    @Test
    void availablePoolSkipsReservedStockAndReportsShortage() {
        Inventory row = row(1, 10, 7, LocalDateTime.now(), null);

        Plan plan = calculator.allocate(List.of(row), 5, false, Pool.AVAILABLE);

        assertThat(plan.isComplete()).isFalse();
        assertThat(plan.allocated()).isEqualTo(3);
        assertThat(plan.shortage()).isEqualTo(2);
    }

    @Test
    void reservedPoolOnlyTakesReservedQuantity() {
        Inventory row = row(1, 10, 4, LocalDateTime.now(), null);

        Plan plan = calculator.allocate(List.of(row), 4, false, Pool.RESERVED);

        assertThat(plan.isComplete()).isTrue();
        assertThat(plan.allocations()).singleElement().extracting(FifoFefoCalculator.Allocation::quantity).isEqualTo(4);
    }
}
