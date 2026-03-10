package com.sakay.ecommerce.dto.request;

import com.sakay.ecommerce.entity.ProductVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVariantRequest {
    @NotNull private ProductVariant.VariantType variantType;
    @NotBlank private String variantValue;
    @NotNull @PositiveOrZero private Integer stockQty;
    private BigDecimal priceModifier = BigDecimal.ZERO;
    private String sku;
}
