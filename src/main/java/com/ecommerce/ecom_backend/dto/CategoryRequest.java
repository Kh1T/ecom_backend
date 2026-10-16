package com.ecommerce.ecom_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CategoryRequest {

    @Schema(description = "Unique category name", example = "Electronics")
    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;

    @Schema(description = "Optional category description", example = "Phones, computers and accessories")
    @Size(max = 2000, message = "Category description must not exceed 2000 characters")
    private String description;
}
