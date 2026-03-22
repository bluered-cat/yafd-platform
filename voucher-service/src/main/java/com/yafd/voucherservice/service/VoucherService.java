package com.yafd.voucherservice.service;

import com.yafd.voucherservice.dto.*;
import com.yafd.voucherservice.entity.Voucher;
import com.yafd.voucherservice.enums.DiscountType;
import com.yafd.voucherservice.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
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

    public ValidateVoucherResponse validate(ValidateVoucherRequest request) {
        Voucher voucher = voucherRepository.findByCode(request.getCode()).orElse(null);

        if (voucher == null) {
            return ValidateVoucherResponse.builder()
                    .valid(false).code(request.getCode()).message("Voucher not found").build();
        }
        if (!voucher.getActive()) {
            return ValidateVoucherResponse.builder()
                    .valid(false).code(request.getCode()).message("Voucher is inactive").build();
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(voucher.getValidFrom()) || now.isAfter(voucher.getValidUntil())) {
            return ValidateVoucherResponse.builder()
                    .valid(false).code(request.getCode()).message("Voucher is expired or not yet valid").build();
        }
        if (voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
            return ValidateVoucherResponse.builder()
                    .valid(false).code(request.getCode()).message("Voucher has reached maximum usage limit").build();
        }
        if (request.getOrderAmount().compareTo(voucher.getMinOrderAmount()) < 0) {
            return ValidateVoucherResponse.builder()
                    .valid(false).code(request.getCode())
                    .message("Order amount below minimum of $" + voucher.getMinOrderAmount()).build();
        }

        BigDecimal discountAmount = calculateDiscount(voucher, request.getOrderAmount());

        return ValidateVoucherResponse.builder()
                .valid(true)
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType().name())
                .discountValue(voucher.getDiscountValue())
                .discountAmount(discountAmount)
                .message("Voucher is valid")
                .build();
    }

    @Transactional
    public RedeemVoucherResponse redeem(RedeemVoucherRequest request) {
        // Pessimistic lock: SELECT ... FOR UPDATE
        Voucher voucher = voucherRepository.findByCodeForUpdate(request.getCode())
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + request.getCode()));

        if (!voucher.getActive()) throw new RuntimeException("Voucher is inactive");

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(voucher.getValidFrom()) || now.isAfter(voucher.getValidUntil()))
            throw new RuntimeException("Voucher is expired");
        if (voucher.getCurrentUsage() >= voucher.getMaxUsage())
            throw new RuntimeException("Voucher has reached maximum usage limit");

        // Redeem: increment usage count
        voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
        voucherRepository.save(voucher);

        return RedeemVoucherResponse.builder()
                .success(true)
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType().name())
                .discountValue(voucher.getDiscountValue().doubleValue())
                .remainingUsage(voucher.getMaxUsage() - voucher.getCurrentUsage())
                .message("Voucher redeemed successfully")
                .build();
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            return orderAmount.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return voucher.getDiscountValue().min(orderAmount);
        }
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