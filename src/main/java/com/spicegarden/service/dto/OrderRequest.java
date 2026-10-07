package com.spicegarden.service.dto;

import java.util.List;

public record OrderRequest(
        String customerName,
        String email,
        String phone,
        String orderType,
        String notes,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(Long itemId, int quantity) {
    }
}
