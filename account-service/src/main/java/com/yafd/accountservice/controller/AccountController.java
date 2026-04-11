package com.yafd.accountservice.controller;

import com.yafd.accountservice.dto.*;
import com.yafd.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> register(@RequestBody RegisterAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.register(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String userId) {
        return ResponseEntity.ok(accountService.getByFirebaseUid(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String userId,
            @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.update(userId, request));
    }
}
