package com.ecommerce.ecom_backend.service;

import com.ecommerce.ecom_backend.dto.BrandRequest;
import com.ecommerce.ecom_backend.dto.BrandResponse;
import com.ecommerce.ecom_backend.entity.Brand;
import com.ecommerce.ecom_backend.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBrandService {

    private final BrandRepository brandRepository;

    public AdminBrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (brandRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Brand already exists");
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());

        Brand savedBrand = brandRepository.save(brand);
        return mapToResponse(savedBrand);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}
