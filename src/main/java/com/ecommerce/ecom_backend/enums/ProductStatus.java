package com.ecommerce.ecom_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "integer", allowableValues = {"1", "2"},
        description = "Product status: 1 = ACTIVE, 2 = DELETED")
public enum ProductStatus {
    ACTIVE(0),
    ARCHIVED(1);

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
