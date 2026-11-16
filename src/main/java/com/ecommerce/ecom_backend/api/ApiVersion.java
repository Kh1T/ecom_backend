package com.ecommerce.ecom_backend.api;

public final class ApiVersion {

    public static final String V1_PREFIX = "/api/v1";
    public static final String V1_AUTH = V1_PREFIX + "/auth";
    public static final String V1_PRODUCTS = V1_PREFIX + "/products";
    public static final String V1_ADMIN = V1_PREFIX + "/admin";
    public static final String V1_ORDERS = V1_PREFIX + "/orders";

    private ApiVersion() {
    }
}
