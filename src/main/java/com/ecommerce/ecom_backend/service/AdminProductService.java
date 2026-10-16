package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.*;
import com.ecommerce.ecom_backend.entity.*;
import com.ecommerce.ecom_backend.enums.ProductStatus;
import com.ecommerce.ecom_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    public AdminProductService(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               BrandRepository brandRepository,
                               ProductImageRepository productImageRepository,
                               ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public AdminProductResponse createProduct(AdminProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(ProductStatus.ACTIVE);

        if (request.getSku() != null && !request.getSku().isBlank()) {
            validateSku(request.getSku(), null);
            product.setSku(request.getSku());
        }

        setCategoryAndBrand(product, request.getCategoryId(), request.getBrandId());

        Product savedProduct = productRepository.save(product);
        return mapToAdminProductResponse(savedProduct);
    }

    @Transactional
    public AdminProductResponse updateProduct(Long id, AdminProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());

        if (request.getSku() != null && !request.getSku().isBlank()) {
            validateSku(request.getSku(), id);
            product.setSku(request.getSku());
        }

        setCategoryAndBrand(product, request.getCategoryId(), request.getBrandId());

        Product updatedProduct = productRepository.save(product);
        return mapToAdminProductResponse(updatedProduct);
    }

    @Transactional
    public AdminProductResponse updateProductStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        return mapToAdminProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete an active product. Archive it first.");
        }

        productRepository.delete(product);
    }

    @Transactional
    public ProductImageResponse addProductImage(Long productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setSortOrder(request.getSortOrder());

        ProductImage savedImage = productImageRepository.save(image);
        return mapToProductImageResponse(savedImage);
    }

    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Image does not belong to this product");
        }

        productImageRepository.delete(image);
    }

    @Transactional
    public ProductVariantResponse createProductVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getSku() != null && !request.getSku().isBlank()) {
            validateVariantSku(request.getSku(), null);
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setVariantName(request.getVariantName());
        variant.setSku(request.getSku());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setStockQuantity(request.getStockQuantity());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return mapToProductVariantResponse(savedVariant);
    }

    @Transactional
    public ProductVariantResponse updateProductVariant(Long productId, Long variantId, ProductVariantUpdateRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to this product");
        }

        if (request.getSku() != null && !request.getSku().isBlank()) {
            validateVariantSku(request.getSku(), variantId);
            variant.setSku(request.getSku());
        }

        variant.setVariantName(request.getVariantName());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setStockQuantity(request.getStockQuantity());

        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return mapToProductVariantResponse(updatedVariant);
    }

    @Transactional(readOnly = true)
    public AdminProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToAdminProductResponse(product);
    }

    private void setCategoryAndBrand(Product product, Long categoryId, Long brandId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategoryEntity(category);
            product.setCategory(category.getName());
        }

        if (brandId != null) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            product.setBrand(brand);
        }
    }

    private void validateSku(String sku, Long excludeProductId) {
        productRepository.findBySku(sku).ifPresent(existing -> {
            if (excludeProductId == null || !existing.getId().equals(excludeProductId)) {
                throw new RuntimeException("SKU already exists");
            }
        });
    }

    private void validateVariantSku(String sku, Long excludeVariantId) {
        productVariantRepository.findBySku(sku).ifPresent(existing -> {
            if (excludeVariantId == null || !existing.getId().equals(excludeVariantId)) {
                throw new RuntimeException("Variant SKU already exists");
            }
        });
    }

    private AdminProductResponse mapToAdminProductResponse(Product product) {
        List<ProductImageResponse> images = productImageRepository
                .findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                .map(this::mapToProductImageResponse)
                .collect(Collectors.toList());

        List<ProductVariantResponse> variants = productVariantRepository
                .findByProductId(product.getId()).stream()
                .map(this::mapToProductVariantResponse)
                .collect(Collectors.toList());

        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getStatus(),
                product.getCategoryEntity() != null ? product.getCategoryEntity().getId() : null,
                product.getCategoryEntity() != null ? product.getCategoryEntity().getName() : product.getCategory(),
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCreatedAt(),
                product.getUpdatedAt(),
                images,
                variants
        );
    }

    private ProductImageResponse mapToProductImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getSortOrder(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }

    private ProductVariantResponse mapToProductVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getVariantName(),
                variant.getSku(),
                variant.getPriceAdjustment(),
                variant.getStockQuantity(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
