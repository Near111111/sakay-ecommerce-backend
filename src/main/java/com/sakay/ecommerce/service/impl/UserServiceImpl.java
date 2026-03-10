package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.dto.request.AddressRequest;
import com.sakay.ecommerce.dto.response.AddressResponse;
import com.sakay.ecommerce.dto.response.UserResponse;
import com.sakay.ecommerce.entity.Address;
import com.sakay.ecommerce.entity.User;
import com.sakay.ecommerce.exception.BadRequestException;
import com.sakay.ecommerce.exception.ResourceNotFoundException;
import com.sakay.ecommerce.repository.AddressRepository;
import com.sakay.ecommerce.repository.UserRepository;
import com.sakay.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public UserResponse getProfile(String email) {
        return UserResponse.from(findUser(email));
    }

    @Override
    public UserResponse updateProfile(String email, UserResponse updates) {
        User user = findUser(email);
        if (updates.getFirstName() != null) user.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) user.setLastName(updates.getLastName());
        if (updates.getPhone() != null) user.setPhone(updates.getPhone());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public List<AddressResponse> getAddresses(String email) {
        User user = findUser(email);
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = findUser(email);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddresses(user.getId());
        }
        Address address = buildAddress(request, user);
        return AddressResponse.from(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String email, UUID addressId, AddressRequest request) {
        User user = findUser(email);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddresses(user.getId());
        }
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setBarangay(request.getBarangay());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setZipCode(request.getZipCode());
        address.setRegion(request.getRegion());
        address.setIsDefault(request.getIsDefault());
        address.setLabel(request.getLabel());
        return AddressResponse.from(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String email, UUID addressId) {
        User user = findUser(email);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Address does not belong to user");
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String email, UUID addressId) {
        User user = findUser(email);
        clearDefaultAddresses(user.getId());
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    private void clearDefaultAddresses(UUID userId) {
        addressRepository.findByUserId(userId).forEach(a -> {
            a.setIsDefault(false);
            addressRepository.save(a);
        });
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Address buildAddress(AddressRequest r, User user) {
        return Address.builder()
                .user(user)
                .fullName(r.getFullName())
                .phone(r.getPhone())
                .street(r.getStreet())
                .barangay(r.getBarangay())
                .city(r.getCity())
                .province(r.getProvince())
                .zipCode(r.getZipCode())
                .region(r.getRegion())
                .isDefault(r.getIsDefault())
                .label(r.getLabel())
                .build();
    }
}