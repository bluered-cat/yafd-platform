package com.yafd.voucherservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RedeemVoucherRequest {
    private String code;
    private Long orderId;
}
