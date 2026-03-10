package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Product.Category category;
    private BigDecimal basePrice;
    private List<String> images;
    private Boolean isActive;
    private String createdAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .category(p.getCategory())
                .basePrice(p.getBasePrice())
                .images(p.getImages())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .build();
    }
}