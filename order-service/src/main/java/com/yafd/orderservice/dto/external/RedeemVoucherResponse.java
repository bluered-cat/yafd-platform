package com.yafd.orderservice.dto.external;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RedeemVoucherResponse {
    private boolean success;
    private int remainingUsage;
    private String message;
}
