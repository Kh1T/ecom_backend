package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        Long orderId,
        String trackingNumber,
        String carrier,
        ShipmentStatus status,
        LocalDateTime estimatedDelivery,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
