package com.ecommerce.ecom_backend.controller;

import com.ecommerce.ecom_backend.dto.*;
import com.ecommerce.ecom_backend.service.AdminProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "Admin Product API", description = "Manage products, images, variants and status")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @PostMapping
    public ResponseEntity<AdminProductResponse> createProduct(
            @Valid @RequestBody AdminProductCreateRequest request) {
        return ResponseEntity.ok(adminProductService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.getProduct(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody AdminProductUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminProductResponse> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProductStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> addProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder) {
        return ResponseEntity.ok(adminProductService.addProductImage(id, file, sortOrder));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        adminProductService.deleteProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ProductVariantResponse> createProductVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(adminProductService.createProductVariant(id, request));
    }

    @PatchMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(
            @PathVariable Long id,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProductVariant(id, variantId, request));
    }
}
