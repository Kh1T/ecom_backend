package com.ecommerce.ecom_backend.controller;

import com.ecommerce.ecom_backend.dto.BrandRequest;
import com.ecommerce.ecom_backend.dto.BrandResponse;
import com.ecommerce.ecom_backend.service.AdminBrandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/brands")
public class AdminBrandController {

    private final AdminBrandService adminBrandService;

    public AdminBrandController(AdminBrandService adminBrandService) {
        this.adminBrandService = adminBrandService;
    }

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(adminBrandService.createBrand(request));
    }
}
