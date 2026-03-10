package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.request.AddressRequest;
import com.sakay.ecommerce.dto.response.UserResponse;
import com.sakay.ecommerce.entity.Address;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getProfile(String email);
    UserResponse updateProfile(String email, UserResponse updates);
    List<Address> getAddresses(String email);
    Address addAddress(String email, AddressRequest request);
    Address updateAddress(String email, UUID addressId, AddressRequest request);
    void deleteAddress(String email, UUID addressId);
    void setDefaultAddress(String email, UUID addressId);
}
