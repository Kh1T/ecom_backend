package com.ecommerce.ecom_backend.dto;

import com.ecommerce.ecom_backend.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    public UserProfileResponse(Long id, String email, String firstName, String lastName, UserRole role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    private UserRole role;
}
