package com.ecommerce.ecom_backend.controller;

import com.ecommerce.ecom_backend.dto.AuthResponse;
import com.ecommerce.ecom_backend.dto.SignInRequest;
import com.ecommerce.ecom_backend.dto.SignUpRequest;
import com.ecommerce.ecom_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }
}
