package com.ecommerce.ecom_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    @NotBlank(message = "Variant name is required")
    private String variantName;

    private String sku;

    @Min(value = 0, message = "Price adjustment must be non-negative")
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQuantity = 0;
}
