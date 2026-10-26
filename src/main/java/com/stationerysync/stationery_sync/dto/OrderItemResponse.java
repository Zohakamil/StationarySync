package com.stationerysync.stationery_sync.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer tariffId,
        String serviceType,
        String paperSize,
        String colorMode,
        int quantity,
        BigDecimal unitRate,
        BigDecimal discountPct,
        BigDecimal subtotal
) {}