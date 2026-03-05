package com.yafd.orderservice.client;

import com.yafd.orderservice.dto.external.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment-url}")
    private String paymentServiceUrl;

    public PaymentResponse processPayment(Long orderId, BigDecimal amount, Long paymentMethodId) {
        String url = paymentServiceUrl + "/api/payments";
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", amount,
                "paymentMethodId", paymentMethodId
        );
        return restTemplate.postForObject(url, request, PaymentResponse.class);
    }
}
