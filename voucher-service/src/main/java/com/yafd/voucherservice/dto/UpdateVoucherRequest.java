package com.yafd.voucherservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateVoucherRequest {
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Integer maxUsage;
    private BigDecimal minOrderAmount;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private Boolean active;
}
