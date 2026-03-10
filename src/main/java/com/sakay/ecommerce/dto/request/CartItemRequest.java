package com.sakay.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequest {
    @NotNull private UUID productId;
    private UUID variantId;
    @Min(1) private int qty;
}
