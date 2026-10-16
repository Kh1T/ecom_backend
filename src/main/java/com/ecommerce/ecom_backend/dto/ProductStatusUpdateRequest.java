package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
