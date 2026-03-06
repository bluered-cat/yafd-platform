package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.AddressRequest;
import com.yafd.accountservice.dto.AddressResponse;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.entity.Address;
import com.yafd.accountservice.repository.AccountRepository;
import com.yafd.accountservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public AddressResponse addAddress(String firebaseUid, AddressRequest request) {
        Account account = accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firebaseUid));

        Address address = Address.builder()
                .account(account)
                .label(request.getLabel())
                .street(request.getStreet())
                .unitNumber(request.getUnitNumber())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        address = addressRepository.save(address);
        return toResponse(address);
    }

    public List<AddressResponse> getAddresses(String firebaseUid) {
        Account account = accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firebaseUid));
        return addressRepository.findByAccountId(account.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddress(String firebaseUid, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        return toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(String firebaseUid, Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));

        if (request.getLabel() != null) address.setLabel(request.getLabel());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getUnitNumber() != null) address.setUnitNumber(request.getUnitNumber());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());

        address = addressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public void deleteAddress(String firebaseUid, Long addressId) {
        addressRepository.deleteById(addressId);
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .street(address.getStreet())
                .unitNumber(address.getUnitNumber())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
