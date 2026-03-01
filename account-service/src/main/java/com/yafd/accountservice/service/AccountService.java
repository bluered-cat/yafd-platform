package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.*;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.enums.AccountRole;
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
                .build();

        account = accountRepository.save(account);
        return toResponse(account);
    }

    public AccountResponse getByFirebaseUid(String firebaseUid) {
        Account account = accountRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firebaseUid));
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .firebaseUid(account.getFirebaseUid())
                .email(account.getEmail())
                .name(account.getName())
                .phone(account.getPhone())
