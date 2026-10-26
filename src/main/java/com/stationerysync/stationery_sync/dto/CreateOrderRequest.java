package com.stationerysync.stationery_sync.dto;

import java.util.List;

public record CreateOrderRequest(List<OrderItemrequest> items) {}