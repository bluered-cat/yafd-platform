package com.yafd.paymentservice.service;

import com.yafd.paymentservice.dto.PaymentRequest;
import com.yafd.paymentservice.dto.PaymentResponse;
import com.yafd.paymentservice.entity.Payment;
import com.yafd.paymentservice.entity.PaymentMethod;
import com.yafd.paymentservice.enums.PaymentMethodType;
import com.yafd.paymentservice.enums.PaymentStatus;
import com.yafd.paymentservice.repository.PaymentMethodRepository;
import com.yafd.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentService paymentService;

    // -------------------------------------------------------------------------
    // processPayment()
    // -------------------------------------------------------------------------

    @Test
    void processPayment_shouldCompletePayment_whenAmountIsWithinLimit() {
        PaymentMethod method = buildPaymentMethod(1L, "uid-001");
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(method));

        Payment pending = buildPayment(1L, 10L, new BigDecimal("20.00"), PaymentStatus.PENDING);
        Payment completed = buildPayment(1L, 10L, new BigDecimal("20.00"), PaymentStatus.COMPLETED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(pending).thenReturn(completed);

        PaymentRequest request = new PaymentRequest(10L, new BigDecimal("20.00"), 1L);
        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void processPayment_shouldFailPayment_whenAmountExceeds500() {
        PaymentMethod method = buildPaymentMethod(1L, "uid-001");
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(method));

        Payment pending = buildPayment(1L, 10L, new BigDecimal("600.00"), PaymentStatus.PENDING);
        Payment failed = buildPayment(1L, 10L, new BigDecimal("600.00"), PaymentStatus.FAILED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(pending).thenReturn(failed);

        PaymentRequest request = new PaymentRequest(10L, new BigDecimal("600.00"), 1L);
        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void processPayment_shouldThrowException_whenPaymentMethodNotFound() {
        when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

        PaymentRequest request = new PaymentRequest(10L, new BigDecimal("20.00"), 99L);

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment method not found");
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnPaymentResponse_whenFound() {
        Payment payment = buildPayment(1L, 10L, new BigDecimal("20.00"), PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getById_shouldThrowException_whenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }

    // -------------------------------------------------------------------------
    // getByOrderId()
    // -------------------------------------------------------------------------

    @Test
    void getByOrderId_shouldReturnPayment_whenFound() {
        Payment payment = buildPayment(1L, 10L, new BigDecimal("20.00"), PaymentStatus.COMPLETED);
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getByOrderId(10L);

        assertThat(response.getOrderId()).isEqualTo(10L);
    }

    @Test
    void getByOrderId_shouldThrowException_whenNotFound() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getByOrderId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for order");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PaymentMethod buildPaymentMethod(Long id, String userId) {
        return PaymentMethod.builder()
                .id(id)
                .userId(userId)
                .type(PaymentMethodType.CREDIT_CARD)
                .label("Personal Card")
                .isDefault(true)
                .build();
    }

    private Payment buildPayment(Long id, Long orderId, BigDecimal amount, PaymentStatus status) {
        PaymentMethod method = buildPaymentMethod(1L, "uid-001");
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .amount(amount)
                .paymentMethod(method)
                .status(status)
                .build();
    }
}
