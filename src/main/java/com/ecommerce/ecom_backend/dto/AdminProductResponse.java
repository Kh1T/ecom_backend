package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        ProductStatus status,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants
) {
}
