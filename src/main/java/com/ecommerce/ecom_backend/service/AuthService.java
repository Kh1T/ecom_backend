package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.AuthResponse;
import com.ecommerce.ecom_backend.dto.SignInRequest;
import com.ecommerce.ecom_backend.dto.SignUpRequest;
import com.ecommerce.ecom_backend.entity.User;
import com.ecommerce.ecom_backend.enums.UserRole;
import com.ecommerce.ecom_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);

        return mapToAuthResponse(savedUser, "User registered successfully");
    }

    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return mapToAuthResponse(user, "Signed in successfully");
    }

    private AuthResponse mapToAuthResponse(User user, String message) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                message
        );
    }
}
