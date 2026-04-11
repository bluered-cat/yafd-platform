package com.yafd.voucherservice.service;

import com.yafd.voucherservice.dto.ValidateVoucherRequest;
import com.yafd.voucherservice.dto.ValidateVoucherResponse;
import com.yafd.voucherservice.dto.RedeemVoucherRequest;
import com.yafd.voucherservice.dto.RedeemVoucherResponse;
import com.yafd.voucherservice.entity.Voucher;
import com.yafd.voucherservice.enums.DiscountType;
import com.yafd.voucherservice.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherService voucherService;

    // -------------------------------------------------------------------------
    // validate() — invalid cases
    // -------------------------------------------------------------------------

    @Test
    void validate_shouldReturnInvalid_whenVoucherNotFound() {
        when(voucherRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        ValidateVoucherRequest request = new ValidateVoucherRequest("UNKNOWN", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("not found");
    }

    @Test
    void validate_shouldReturnInvalid_whenVoucherIsInactive() {
        Voucher voucher = buildValidVoucher("SAVE10");
        voucher.setActive(false);
        when(voucherRepository.findByCode("SAVE10")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SAVE10", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("inactive");
    }

    @Test
    void validate_shouldReturnInvalid_whenVoucherIsExpired() {
        Voucher voucher = buildValidVoucher("SAVE10");
        voucher.setValidUntil(OffsetDateTime.now().minusDays(1));
        when(voucherRepository.findByCode("SAVE10")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SAVE10", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("expired");
    }

    @Test
    void validate_shouldReturnInvalid_whenMaxUsageReached() {
        Voucher voucher = buildValidVoucher("SAVE10");
        voucher.setMaxUsage(10);
        voucher.setCurrentUsage(10);
        when(voucherRepository.findByCode("SAVE10")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SAVE10", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("maximum usage");
    }

    @Test
    void validate_shouldReturnInvalid_whenOrderAmountBelowMinimum() {
        Voucher voucher = buildValidVoucher("SAVE10");
        voucher.setMinOrderAmount(new BigDecimal("30.00"));
        when(voucherRepository.findByCode("SAVE10")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SAVE10", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).containsIgnoringCase("minimum");
    }

    // -------------------------------------------------------------------------
    // validate() — discount calculation
    // -------------------------------------------------------------------------

    @Test
    void validate_shouldCalculatePercentageDiscount_whenVoucherIsValid() {
        Voucher voucher = buildValidVoucher("FLASH10");
        voucher.setDiscountType(DiscountType.PERCENTAGE);
        voucher.setDiscountValue(new BigDecimal("10")); // 10%
        when(voucherRepository.findByCode("FLASH10")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("FLASH10", new BigDecimal("50.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00")); // 10% of $50
    }

    @Test
    void validate_shouldCalculateFixedDiscount_whenVoucherIsValid() {
        Voucher voucher = buildValidVoucher("SAVE5");
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("5.00"));
        when(voucherRepository.findByCode("SAVE5")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SAVE5", new BigDecimal("20.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void validate_shouldCapFixedDiscountAtOrderAmount() {
        Voucher voucher = buildValidVoucher("BIGSAVE");
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("50.00")); // discount > order amount
        when(voucherRepository.findByCode("BIGSAVE")).thenReturn(Optional.of(voucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("BIGSAVE", new BigDecimal("10.00"));
        ValidateVoucherResponse response = voucherService.validate(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00")); // capped at order amount
    }

    // -------------------------------------------------------------------------
    // redeem()
    // -------------------------------------------------------------------------

    @Test
    void redeem_shouldIncrementUsageCount() {
        Voucher voucher = buildValidVoucher("SAVE10");
        voucher.setCurrentUsage(2);
        when(voucherRepository.findByCodeForUpdate("SAVE10")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        RedeemVoucherRequest request = new RedeemVoucherRequest("SAVE10", 1L);
        RedeemVoucherResponse response = voucherService.redeem(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(voucher.getCurrentUsage()).isEqualTo(3);
    }

    @Test
    void redeem_shouldThrowException_whenVoucherNotFound() {
        when(voucherRepository.findByCodeForUpdate("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.redeem(new RedeemVoucherRequest("MISSING", 1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Voucher not found");
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldThrowException_whenVoucherNotFound() {
        when(voucherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Voucher not found");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Voucher buildValidVoucher(String code) {
        return Voucher.builder()
                .id(1L)
                .code(code)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("5.00"))
                .maxUsage(100)
                .currentUsage(0)
                .minOrderAmount(BigDecimal.ZERO)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .active(true)
                .build();
    }
}
