package com.yafd.paymentservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private Long paymentMethodId;
    private String status;
    private String transactionRef;
    private String failureReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
