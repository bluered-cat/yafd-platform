package com.yafd.accountservice.controller;

import com.yafd.accountservice.dto.AddressRequest;
import com.yafd.accountservice.dto.AddressResponse;
import com.yafd.accountservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable String userId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable String userId) {
        return ResponseEntity.ok(addressService.getAddresses(userId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable String userId,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getAddress(userId, addressId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable String userId,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String userId,
            @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
