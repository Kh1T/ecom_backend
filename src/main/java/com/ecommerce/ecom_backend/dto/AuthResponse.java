package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.UserRole;

public record AuthResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        String message,
        String accessToken,
        String refreshToken
) {
    public AuthResponse(Long id, String email, String firstName, String lastName, UserRole role, String message) {
        this(id, email, firstName, lastName, role, message, null, null);
    }
}
