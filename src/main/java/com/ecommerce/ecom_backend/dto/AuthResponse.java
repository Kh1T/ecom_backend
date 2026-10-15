package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.UserRole;
import lombok.Data;

@Data
public class AuthResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String message;
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }

    public AuthResponse(Long id, String email, String firstName, String lastName, UserRole role, String message) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.message = message;
    }

    public AuthResponse(Long id, String email, String firstName, String lastName, UserRole role, String message, String accessToken, String refreshToken) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
