package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.Address;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddressResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String street;
    private String barangay;
    private String city;
    private String province;
    private String zipCode;
    private String region;
    private Boolean isDefault;
    private Address.AddressLabel label;
    private String createdAt;

    public static AddressResponse from(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .street(a.getStreet())
                .barangay(a.getBarangay())
                .city(a.getCity())
                .province(a.getProvince())
                .zipCode(a.getZipCode())
                .region(a.getRegion())
                .isDefault(a.getIsDefault())
                .label(a.getLabel())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null)
                .build();
    }
}