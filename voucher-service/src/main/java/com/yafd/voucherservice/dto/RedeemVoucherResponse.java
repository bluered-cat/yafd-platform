package com.yafd.voucherservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RedeemVoucherResponse {
    private boolean success;
    private String code;
    private String discountType;
    private Double discountValue;
    private Integer remainingUsage;
    private String message;
}
