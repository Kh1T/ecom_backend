package com.ecommerce.ecom_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Category details")
public record CategoryResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "Electronics")
        String name,
        @Schema(example = "Phones, computers and accessories")
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
