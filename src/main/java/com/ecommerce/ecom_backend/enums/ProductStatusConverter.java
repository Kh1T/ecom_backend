package com.ecommerce.ecom_backend.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter implements AttributeConverter<ProductStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProductStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public ProductStatus convertToEntityAttribute(Integer value) {
        return value == null ? null : ProductStatus.fromValue(value);
    }
}
