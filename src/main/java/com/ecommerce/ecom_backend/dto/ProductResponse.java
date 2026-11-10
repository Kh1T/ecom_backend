package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        ProductStatus status,
        Long categoryId,
        String categoryName,
        String category,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
