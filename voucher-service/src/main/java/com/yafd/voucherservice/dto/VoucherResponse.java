package com.yafd.voucherservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Integer maxUsage;
    private Integer currentUsage;
    private BigDecimal minOrderAmount;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
