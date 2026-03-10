package com.sakay.ecommerce.dto.request;

import com.sakay.ecommerce.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank private String name;
    @NotBlank private String slug;
    private String description;
    @NotNull private Product.Category category;
    @NotNull @Positive private BigDecimal basePrice;
    private List<String> images;
}
