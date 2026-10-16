package com.ecommerce.ecom_backend.repository;

import com.ecommerce.ecom_backend.entity.Product;
import com.ecommerce.ecom_backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(
            String name, ProductStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.categoryEntity = null WHERE p.categoryEntity.id = :categoryId")
    int clearCategory(@Param("categoryId") Long categoryId);
}
