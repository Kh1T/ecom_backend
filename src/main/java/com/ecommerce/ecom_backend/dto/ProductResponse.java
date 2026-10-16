package com.ecommerce.ecom_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
