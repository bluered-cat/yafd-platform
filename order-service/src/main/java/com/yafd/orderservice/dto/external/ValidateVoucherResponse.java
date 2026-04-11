package com.yafd.orderservice.dto.external;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ValidateVoucherResponse {
    private boolean valid;
    private BigDecimal discountAmount;
    private String message;
}
