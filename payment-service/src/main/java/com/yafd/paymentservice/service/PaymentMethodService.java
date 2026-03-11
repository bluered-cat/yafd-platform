package com.yafd.paymentservice.service;

import com.yafd.paymentservice.dto.PaymentMethodRequest;
import com.yafd.paymentservice.dto.PaymentMethodResponse;
import com.yafd.paymentservice.entity.PaymentMethod;
import com.yafd.paymentservice.enums.PaymentMethodType;
import com.yafd.paymentservice.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository repository;

    public PaymentMethodResponse addPaymentMethod(PaymentMethodRequest request) {
        PaymentMethod method = PaymentMethod.builder()
                .userId(request.getUserId())
                .type(PaymentMethodType.valueOf(request.getType()))
                .label(request.getLabel())
                .lastFour(request.getLastFour())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
        return toResponse(repository.save(method));
    }

    public List<PaymentMethodResponse> getByUserId(String userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentMethodResponse getById(Long id) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + id));
        return toResponse(method);
    }

    public PaymentMethodResponse update(Long id, PaymentMethodRequest request) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + id));
        if (request.getLabel() != null) method.setLabel(request.getLabel());
        if (request.getType() != null) method.setType(PaymentMethodType.valueOf(request.getType()));
        if (request.getLastFour() != null) method.setLastFour(request.getLastFour());
        if (request.getIsDefault() != null) method.setIsDefault(request.getIsDefault());
        return toResponse(repository.save(method));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Payment method not found: " + id);
        }
        repository.deleteById(id);
    }

    private PaymentMethodResponse toResponse(PaymentMethod m) {
        return PaymentMethodResponse.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .type(m.getType().name())
                .label(m.getLabel())
                .lastFour(m.getLastFour())
                .isDefault(m.getIsDefault())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
