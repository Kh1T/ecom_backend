package com.ecommerce.ecom_backend.repository;

import com.ecommerce.ecom_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    Optional<Product> findBySku(String sku);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.category = :categoryName WHERE p.categoryEntity.id = :categoryId")
    int updateCategoryName(@Param("categoryId") Long categoryId,
                           @Param("categoryName") String categoryName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.categoryEntity = null, p.category = null " +
            "WHERE p.categoryEntity.id = :categoryId")
    int clearCategory(@Param("categoryId") Long categoryId);
}
