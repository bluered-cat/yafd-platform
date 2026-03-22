package com.yafd.voucherservice.service;

import com.yafd.voucherservice.dto.*;
import com.yafd.voucherservice.entity.Voucher;
import com.yafd.voucherservice.enums.DiscountType;
import com.yafd.voucherservice.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    @Transactional
    public VoucherResponse create(CreateVoucherRequest request) {
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountType(DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .maxUsage(request.getMaxUsage())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();
        voucher = voucherRepository.save(voucher);
        return toResponse(voucher);
    }

    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VoucherResponse getById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + id));
        return toResponse(voucher);
    }

    @Transactional
    public VoucherResponse update(Long id, UpdateVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + id));

        if (request.getDescription() != null) voucher.setDescription(request.getDescription());
        if (request.getDiscountType() != null) voucher.setDiscountType(DiscountType.valueOf(request.getDiscountType()));
        if (request.getDiscountValue() != null) voucher.setDiscountValue(request.getDiscountValue());
        if (request.getMaxUsage() != null) voucher.setMaxUsage(request.getMaxUsage());
        if (request.getMinOrderAmount() != null) voucher.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getValidFrom() != null) voucher.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) voucher.setValidUntil(request.getValidUntil());
        if (request.getActive() != null) voucher.setActive(request.getActive());

        voucher = voucherRepository.save(voucher);
        return toResponse(voucher);
    }

    @Transactional
    public void delete(Long id) {
        voucherRepository.deleteById(id);
    }

    private VoucherResponse toResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType().name())
                .discountValue(voucher.getDiscountValue())
                .maxUsage(voucher.getMaxUsage())
                .currentUsage(voucher.getCurrentUsage())
                .minOrderAmount(voucher.getMinOrderAmount())
                .validFrom(voucher.getValidFrom())
                .validUntil(voucher.getValidUntil())
                .active(voucher.getActive())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}