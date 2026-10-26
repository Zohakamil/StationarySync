package com.stationerysync.stationery_sync.service;

import com.stationerysync.stationery_sync.dto.*;
import com.stationerysync.stationery_sync.entity.OrderItem;
import com.stationerysync.stationery_sync.entity.SalesOrder;
import com.stationerysync.stationery_sync.exception.NotFoundException;
import com.stationerysync.stationery_sync.repository.OrderItemRepository;
import com.stationerysync.stationery_sync.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final PricingService pricingService;
    private final SalesOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    public OrderService(PricingService pricingService,
                        SalesOrderRepository orderRepo,
                        OrderItemRepository itemRepo) {
        this.pricingService = pricingService;
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    /** Price-only (used by POST /api/price). No DB write. */
    public PriceResponse priceOrder(CreateOrderRequest request) {
        List<PricedItem> priced = priceAll(request);
        BigDecimal total = sumTotals(priced);
        return new PriceResponse(toItemResponses(priced), total);
    }

    /** FR-7 to FR-9: creates the order + items in ONE atomic transaction. */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1) price everything up front — if any item is invalid, we throw
        //    here and NOTHING has been written yet (natural rollback).
        List<PricedItem> priced = priceAll(request);

        // 2) BR-6: order total = sum of item subtotals
        BigDecimal total = sumTotals(priced);

        // 3) Insert the header row (Sales_Orders)
        SalesOrder order = new SalesOrder();
        order.setOrderTimestamp(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setPaymentMode("CASH");   // FR-8: cash only in this version
        order.setStatus("COMPLETED");   // FR-8
        order = orderRepo.save(order);  // returns the entity with generated order_id
        Integer orderId = order.getOrderId();

        // 4) Insert one row per item (Order_Items)
        List<OrderItem> savedItems = new ArrayList<>();
        for (PricedItem p : priced) {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setTariffId(p.tariff().getTariffId());
            item.setQuantity(p.quantity());
            item.setUnitRate(p.unitRate());       // BR-5: rate is copied
            item.setDiscountPct(p.discountPct()); // BR-5: discount copied
            item.setSubtotal(p.subtotal());
            savedItems.add(itemRepo.save(item));
        }

        // 5) Build and return the receipt DTO
        return new OrderResponse(
                orderId,
                order.getOrderTimestamp(),
                toItemResponses(priced),
                total,
                order.getPaymentMode(),
                order.getStatus()
        );
    }

    /** FR-10: search by ID or date range */
    public List<OrderResponse> search(Integer orderId, LocalDateTime from, LocalDateTime to) {
        if (orderId != null) {
            SalesOrder o = orderRepo.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
            return List.of(toOrderResponse(o));
        }
        if (from == null || to == null) {
            // If no date range, default to today
            LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
            from = start;
            to = start.plusDays(1);
        }
        return orderRepo.findByOrderTimestampBetweenOrderByOrderTimestampDesc(from, to)
                .stream().map(this::toOrderResponse).toList();
    }

    /** FR-12: cancel — status change only, no deletion (BR-8) */
    @Transactional
    public void cancelOrder(Integer orderId) {
        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
        order.setStatus("CANCELLED");
        // No save() needed — Hibernate detects the change on commit (dirty checking)
    }

    // --- helpers --------------------------------------------------------

    private List<PricedItem> priceAll(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item"); // BR-7
        }
        List<PricedItem> priced = new ArrayList<>();
        for (OrderItemrequest item : request.items())
            priced.add(pricingService.priceItem(item.tariffId(), item.quantity()));
        return priced;
    }

    private BigDecimal sumTotals(List<PricedItem> priced) {
        return priced.stream()
                .map(PricedItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItemResponse> toItemResponses(List<PricedItem> priced) {
        return priced.stream().map(p -> new OrderItemResponse(
                p.tariff().getTariffId(),
                p.tariff().getServiceType(),
                p.tariff().getPaperSize(),
                p.tariff().getColorMode(),
                p.quantity(),
                p.unitRate(),
                p.discountPct(),
                p.subtotal()
        )).toList();
    }

    private OrderResponse toOrderResponse(SalesOrder order) {
        List<OrderItem> items = itemRepo.findByOrderId(order.getOrderId());
        List<OrderItemResponse> itemResponses = items.stream().map(i -> new OrderItemResponse(
                i.getTariffId(), null, null, null,   // tariff fields not needed for search
                i.getQuantity(), i.getUnitRate(), i.getDiscountPct(), i.getSubtotal()
        )).toList();
        return new OrderResponse(
                order.getOrderId(), order.getOrderTimestamp(),
                itemResponses, order.getTotalAmount(),
                order.getPaymentMode(), order.getStatus()
        );
    }
}