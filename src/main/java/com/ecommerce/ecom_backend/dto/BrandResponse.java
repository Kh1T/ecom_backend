package com.ecommerce.ecom_backend.dto;

import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
