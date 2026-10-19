package com.ecommerce.ecom_backend.enums;

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
