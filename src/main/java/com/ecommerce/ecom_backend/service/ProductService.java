package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.ProductCreateRequest;
import com.ecommerce.ecom_backend.dto.ProductResponse;
import com.ecommerce.ecom_backend.entity.Category;
import com.ecommerce.ecom_backend.entity.Product;
import com.ecommerce.ecom_backend.entity.ProductImage;
import com.ecommerce.ecom_backend.enums.ProductStatus;
import com.ecommerce.ecom_backend.exception.DuplicateResourceException;
import com.ecommerce.ecom_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecom_backend.repository.CategoryRepository;
import com.ecommerce.ecom_backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findActiveProduct(id);
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndStatus(
                        query, ProductStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (request.getSku() != null && !request.getSku().isBlank()
                && productRepository.findBySku(request.getSku()).isPresent()) {
            throw new DuplicateResourceException("SKU already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setStatus(ProductStatus.ACTIVE);
        setCategory(product, request.getCategoryId());

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductCreateRequest request) {
        Product product = findActiveProduct(id);

        if (request.getSku() != null && !request.getSku().isBlank()) {
            productRepository.findBySku(request.getSku()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("SKU already exists");
                }
            });
            product.setSku(request.getSku());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        setCategory(product, request.getCategoryId());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findActiveProduct(id);
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> imageUrls = product.getImages() == null
                ? Collections.emptyList()
                : product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getStatus(),
                product.getCategoryEntity() != null ? product.getCategoryEntity().getId() : null,
                product.getCategoryEntity() != null ? product.getCategoryEntity().getName() : null,
                product.getCategory(),
                imageUrls,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void setCategory(Product product, Long categoryId) {
        if (categoryId == null) {
            product.setCategoryEntity(null);
            return;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategoryEntity(category);
    }
}
