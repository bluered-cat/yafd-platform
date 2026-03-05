package com.yafd.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private String userId;
    private String deliveryAddress;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String voucherCode;
    private Long paymentMethodId;
    private String status;
    private Long paymentId;
    private Long riderId;
    private String riderName;
    private List<OrderItemResponse> items;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
