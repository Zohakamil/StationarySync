package com.stationerysync.stationery_sync.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        LocalDateTime orderTimestamp,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String paymentMode,
        String status
) {}