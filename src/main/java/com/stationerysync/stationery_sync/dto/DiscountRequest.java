package com.stationerysync.stationery_sync.dto;

import java.math.BigDecimal;

public record DiscountRequest(
        String serviceType,
        Integer minQuantity,
        BigDecimal discountPct,
        Boolean isActive
) {}