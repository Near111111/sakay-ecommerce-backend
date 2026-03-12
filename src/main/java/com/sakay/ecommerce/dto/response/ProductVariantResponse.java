package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.ProductVariant;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductVariantResponse {

    private UUID id;
    private UUID productId;
    private String variantType;
    private String variantValue;
    private Integer stockQty;
    private BigDecimal priceModifier;
    private String sku;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    public static ProductVariantResponse from(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .productId(v.getProduct().getId())
                .variantType(v.getVariantType().name())
                .variantValue(v.getVariantValue())
                .stockQty(v.getStockQty())
                .priceModifier(v.getPriceModifier())
                .sku(v.getSku())
                .isActive(v.getIsActive())
                .createdAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : null)
                .updatedAt(v.getUpdatedAt() != null ? v.getUpdatedAt().toString() : null)
                .build();
    }
}