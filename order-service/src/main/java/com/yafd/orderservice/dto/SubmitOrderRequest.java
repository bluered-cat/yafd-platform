package com.yafd.orderservice.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmitOrderRequest {
    private String userId;
    private List<OrderItemRequest> items;
    private String voucherCode;
    private Long addressId;
    private Long paymentMethodId;
}
