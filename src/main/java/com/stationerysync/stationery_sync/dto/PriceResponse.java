package com.stationerysync.stationery_sync.dto;

import java.math.BigDecimal;
import java.util.List;

public record PriceResponse(List<OrderItemResponse> items, BigDecimal totalAmount) {}