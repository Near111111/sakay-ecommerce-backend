package com.sakay.ecommerce.dto.request;

import com.sakay.ecommerce.entity.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank private String fullName;
    @NotBlank private String phone;
    @NotBlank private String street;
    private String barangay;
    @NotBlank private String city;
    @NotBlank private String province;
    @NotBlank private String zipCode;
    private String region;
    private Boolean isDefault = false;
    private Address.AddressLabel label = Address.AddressLabel.HOME;
}
