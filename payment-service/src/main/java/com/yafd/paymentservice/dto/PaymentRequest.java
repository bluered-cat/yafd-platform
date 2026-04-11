package com.yafd.paymentservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private Long paymentMethodId;
}
