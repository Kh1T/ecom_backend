package com.ecommerce.ecom_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "integer", allowableValues = {"1", "2"},
        description = "Product status: 1 = ACTIVE, 2 = DELETED")
public enum ProductStatus {
    ACTIVE(1),
    DELETED(2);

    private final int code;

    ProductStatus(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static ProductStatus fromCode(int code) {
        for (ProductStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Product status must be 1 (ACTIVE) or 2 (DELETED)");
    }
}
