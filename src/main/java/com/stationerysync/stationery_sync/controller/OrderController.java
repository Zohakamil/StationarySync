package com.stationerysync.stationery_sync.controller;

import com.stationerysync.stationery_sync.dto.CreateOrderRequest;
import com.stationerysync.stationery_sync.dto.OrderResponse;
import com.stationerysync.stationery_sync.dto.PriceResponse;
import com.stationerysync.stationery_sync.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // FR-2 to FR-5: price without saving
    @PostMapping("/price")
    public PriceResponse price(@RequestBody CreateOrderRequest request) {
        return orderService.priceOrder(request);
    }

    // FR-7 to FR-9: create order (transaction T1)
    @PostMapping("/orders")
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // FR-9: fetch one order for the receipt
    @GetMapping("/orders/{id}")
    public OrderResponse get(@PathVariable Integer id) {
        return orderService.search(id, null, null).get(0);
    }

    // FR-10: search by id or date
    @GetMapping("/orders")
    public List<OrderResponse> search(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime from = (date == null) ? null : date.atStartOfDay();
        LocalDateTime to   = (date == null) ? null : date.plusDays(1).atStartOfDay();
        return orderService.search(id, from, to);
    }

    // FR-12: cancel
    @PutMapping("/orders/{id}/cancel")
    public void cancel(@PathVariable Integer id) {
        orderService.cancelOrder(id);
    }
}