package com.yafd.paymentservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentMethodRequest {
    private String userId;
    private String type;
    private String label;
    private String lastFour;
    private Boolean isDefault;
}
