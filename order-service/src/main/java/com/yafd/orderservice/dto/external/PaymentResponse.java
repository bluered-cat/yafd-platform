package com.yafd.orderservice.dto.external;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private Long paymentMethodId;
    private String status;
    private String transactionRef;
    private String failureReason;
}
