package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.*;
import com.ecommerce.ecom_backend.entity.PasswordResetToken;
import com.ecommerce.ecom_backend.entity.RefreshToken;
import com.ecommerce.ecom_backend.entity.User;
import com.ecommerce.ecom_backend.enums.UserRole;
import com.ecommerce.ecom_backend.exception.BusinessException;
import com.ecommerce.ecom_backend.exception.DuplicateResourceException;
import com.ecommerce.ecom_backend.exception.ErrorCode;
import com.ecommerce.ecom_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecom_backend.repository.PasswordResetTokenRepository;
import com.ecommerce.ecom_backend.repository.RefreshTokenRepository;
import com.ecommerce.ecom_backend.repository.UserRepository;
import com.ecommerce.ecom_backend.security.CustomUserDetailsService;
import com.ecommerce.ecom_backend.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser, "User registered successfully");
    }

    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        return buildAuthResponse(user, "Signed in successfully");
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenValue = request.getRefreshToken();

        if (!jwtUtil.isRefreshToken(tokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh token has expired");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, "Token refreshed successfully");
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public UserProfileResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES));
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password reset token generated: " + token);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Reset token has already been used");
        }

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password reset successfully");
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshTokenValue = jwtUtil.generateRefreshToken(userDetails);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                message,
                accessToken,
                refreshTokenValue
        );
    }
}
