package com.yafd.paymentservice.service;

import com.yafd.paymentservice.dto.PaymentRequest;
import com.yafd.paymentservice.dto.PaymentResponse;
import com.yafd.paymentservice.entity.Payment;
import com.yafd.paymentservice.entity.PaymentMethod;
import com.yafd.paymentservice.enums.PaymentStatus;
import com.yafd.paymentservice.repository.PaymentMethodRepository;
import com.yafd.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + request.getPaymentMethodId()));

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(method)
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        // Simulate payment processing delay (200-500ms)
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(200, 501));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate payment result: amounts > $500 fail
        if (request.getAmount().compareTo(new BigDecimal("500")) > 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds (simulated)");
        } else {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
        return toResponse(payment);
    }

    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .amount(p.getAmount())
                .paymentMethodId(p.getPaymentMethod().getId())
                .status(p.getStatus().name())
                .transactionRef(p.getTransactionRef())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
