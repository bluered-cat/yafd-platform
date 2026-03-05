package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.*;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.enums.AccountRole;
import com.yafd.accountservice.enums.VehicleType;
import com.yafd.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse register(RegisterAccountRequest request) {
        Account account = Account.builder()
                .firebaseUid(request.getFirebaseUid())
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(AccountRole.valueOf(request.getRole()))
                .vehicleType(request.getVehicleType() != null ?
                        VehicleType.valueOf(request.getVehicleType()) : null)
                .isAvailable(AccountRole.valueOf(request.getRole()) == AccountRole.RIDER ? true : null)
                .build();

        account = accountRepository.save(account);
        return toResponse(account);
    }

    public AccountResponse getByFirebaseUid(String firebaseUid) {
        Account account = accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firebaseUid));
        return toResponse(account);
    }

    @Transactional
    public AccountResponse update(String firebaseUid, UpdateAccountRequest request) {
        Account account = accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firebaseUid));

        if (request.getName() != null) account.setName(request.getName());
        if (request.getPhone() != null) account.setPhone(request.getPhone());
        if (request.getEmail() != null) account.setEmail(request.getEmail());

        account = accountRepository.save(account);
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .firebaseUid(account.getFirebaseUid())
                .email(account.getEmail())
                .name(account.getName())
                .phone(account.getPhone())
                .role(account.getRole().name())
                .vehicleType(account.getVehicleType() != null ? account.getVehicleType().name() : null)
                .isAvailable(account.getIsAvailable())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
