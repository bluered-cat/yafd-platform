package com.yafd.voucherservice.controller;

import com.yafd.voucherservice.dto.*;
import com.yafd.voucherservice.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<VoucherResponse> create(@RequestBody CreateVoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> getById(@PathVariable Long voucherId) {
        return ResponseEntity.ok(voucherService.getById(voucherId));
    }

    @PutMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> update(
            @PathVariable Long voucherId,
            @RequestBody UpdateVoucherRequest request) {
        return ResponseEntity.ok(voucherService.update(voucherId, request));
    }

    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> delete(@PathVariable Long voucherId) {
        voucherService.delete(voucherId);
        return ResponseEntity.noContent().build();
    }
}