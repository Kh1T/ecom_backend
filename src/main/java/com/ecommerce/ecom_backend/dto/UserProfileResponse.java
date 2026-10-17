package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.UserRole;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {
}
