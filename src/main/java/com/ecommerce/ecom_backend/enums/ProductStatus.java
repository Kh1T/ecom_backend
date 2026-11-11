package com.ecommerce.ecom_backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "integer", allowableValues = {"0", "1"},
        description = "Product status: 0 = ACTIVE, 1 = DELETED")
public enum ProductStatus {
    ACTIVE(0),
    DELETED(1);

    private final int value;

    ProductStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProductStatus fromValue(int value) {
        for (ProductStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus value: " + value);
    }
}
