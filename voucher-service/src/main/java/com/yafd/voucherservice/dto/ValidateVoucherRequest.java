package com.yafd.voucherservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidateVoucherRequest {
    private String code;
    private BigDecimal orderAmount;
}
