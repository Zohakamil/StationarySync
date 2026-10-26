package com.stationerysync.stationery_sync.dto;

import java.math.BigDecimal;

public record TariffRequest(
        String serviceType,
        String paperSize,
        String colorMode,
        BigDecimal unitRate,
        Boolean isActive
) {}