package com.sakay.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CheckoutRequest {
    @NotNull private UUID addressId;
    private BigDecimal shippingFee;
}
