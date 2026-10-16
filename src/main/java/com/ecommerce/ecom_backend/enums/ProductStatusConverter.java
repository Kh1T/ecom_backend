package com.ecommerce.ecom_backend.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter implements AttributeConverter<ProductStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(ProductStatus status) {
        return status == null ? null : (short) status.getCode();
    }

    @Override
    public ProductStatus convertToEntityAttribute(Short code) {
        return code == null ? null : ProductStatus.fromCode(code.intValue());
    }
}
