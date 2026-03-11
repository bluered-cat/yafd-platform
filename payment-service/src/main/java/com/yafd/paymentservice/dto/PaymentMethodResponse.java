package com.yafd.paymentservice.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentMethodResponse {
    private Long id;
    private String userId;
    private String type;
    private String label;
    private String lastFour;
    private Boolean isDefault;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
