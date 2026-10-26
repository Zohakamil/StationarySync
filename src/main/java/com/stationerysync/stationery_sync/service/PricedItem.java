package com.stationerysync.stationery_sync.service;

import com.stationerysync.stationery_sync.entity.ServiceTariff;

import java.math.BigDecimal;

public record PricedItem(
        ServiceTariff tariff,
        int quantity,
        BigDecimal unitRate,
        BigDecimal discountPct,
        BigDecimal subtotal
) {}