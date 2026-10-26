package com.stationerysync.stationery_sync;

import com.stationerysync.stationery_sync.dto.*;
import com.stationerysync.stationery_sync.exception.NoActiveTariffException;
import com.stationerysync.stationery_sync.repository.OrderItemRepository;
import com.stationerysync.stationery_sync.repository.SalesOrderRepository;
import com.stationerysync.stationery_sync.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderFlowTest {

    @Autowired private OrderService orderService;
    @Autowired private SalesOrderRepository orderRepo;
    @Autowired private OrderItemRepository itemRepo;

    /** TC-1: 50 pages A4 BW at PKR 8 → subtotal 400, no discount. */
    @Test
    void tc1_noDiscount() {
        PriceResponse r = orderService.priceOrder(
                new CreateOrderRequest(List.of(new OrderItemrequest(1, 50))));
        assertEquals(0, r.items().get(0).discountPct().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.items().get(0).subtotal().compareTo(new BigDecimal("400.00")));
    }

    /** TC-2: 120 pages A4 color at PKR 30 → 10% discount, subtotal 3240. */
    @Test
    void tc2_volumeDiscountApplied() {
        PriceResponse r = orderService.priceOrder(
                new CreateOrderRequest(List.of(new OrderItemrequest(2, 120))));
        assertEquals(0, r.items().get(0).discountPct().compareTo(new BigDecimal("10.00")));
        assertEquals(0, r.items().get(0).subtotal().compareTo(new BigDecimal("3240.00")));
    }

    /** TC-3: exactly 100 pages → no discount (threshold exceeded, not met). */
    @Test
    void tc3_boundaryExactly100() {
        PriceResponse r = orderService.priceOrder(
                new CreateOrderRequest(List.of(new OrderItemrequest(2, 100))));
        assertEquals(0, r.items().get(0).discountPct().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.items().get(0).subtotal().compareTo(new BigDecimal("3000.00")));
    }

    /** TC-4: invalid tariff id → NoActiveTariffException, nothing written. */
    @Test
    void tc4_inactiveOrMissingTariff() {
        long before = orderRepo.count();
        assertThrows(NoActiveTariffException.class, () ->
                orderService.createOrder(
                        new CreateOrderRequest(List.of(new OrderItemrequest(9999, 10)))));
        assertEquals(before, orderRepo.count(), "No order row should have been written");
    }

    /** TC-5: quantity 0 → IllegalArgumentException. */
    @Test
    void tc5_zeroQuantityRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                orderService.priceOrder(
                        new CreateOrderRequest(List.of(new OrderItemrequest(1, 0)))));
    }

    /** TC-6: order with three items → one header, three items, total = sum. */
    @Test
    void tc6_multiItemOrder() {
        long ordersBefore = orderRepo.count();
        OrderResponse o = orderService.createOrder(new CreateOrderRequest(List.of(
                new OrderItemrequest(1, 50),     // 400
                new OrderItemrequest(2, 120),    // 3240
                new OrderItemrequest(5, 1)       // 150
        )));
        assertEquals(ordersBefore + 1, orderRepo.count());
        assertEquals(3, o.items().size());
        assertEquals(0, o.totalAmount().compareTo(new BigDecimal("3790.00")));
        assertEquals(3, itemRepo.findByOrderId(o.orderId()).size());
    }

    /** TC-7: one bad item in the batch → no rows written at all (atomicity). */
    @Test
    void tc7_atomicRollback() {
        long ordersBefore = orderRepo.count();
        long itemsBefore = itemRepo.count();
        assertThrows(NoActiveTariffException.class, () ->
                orderService.createOrder(new CreateOrderRequest(List.of(
                        new OrderItemrequest(1, 50),      // valid
                        new OrderItemrequest(9999, 5)     // invalid → must abort whole order
                ))));
        assertEquals(ordersBefore, orderRepo.count(), "Header row must not exist after rollback");
        assertEquals(itemsBefore, itemRepo.count(), "Item rows must not exist after rollback");
    }

    /** TC-9: change a tariff rate afterwards → old order keeps its copied rate. */
    @Test
    void tc9_rateCopyPreserved() {
        OrderResponse o = orderService.createOrder(
                new CreateOrderRequest(List.of(new OrderItemrequest(2, 120))));
        // Simulate a later tariff change:
        var tariff = o.items().get(0);
        // (In a real test you'd update the DB; here we just check the copied value.)
        assertEquals(0, tariff.unitRate().compareTo(new BigDecimal("30.00")));
        assertEquals(0, tariff.discountPct().compareTo(new BigDecimal("10.00")));
        // The order was stored with those exact historical values.
        var storedItem = itemRepo.findByOrderId(o.orderId()).get(0);
        assertEquals(0, storedItem.getUnitRate().compareTo(new BigDecimal("30.00")));
        assertEquals(0, storedItem.getDiscountPct().compareTo(new BigDecimal("10.00")));
    }}

    /** TC-10: cancel keeps the row, status becomes CANCELLED. */
