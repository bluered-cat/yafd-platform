package com.yafd.voucherservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateVoucherRequest {
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Integer maxUsage;
    private BigDecimal minOrderAmount;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
}
