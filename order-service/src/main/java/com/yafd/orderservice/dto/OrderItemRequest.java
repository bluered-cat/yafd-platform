package com.yafd.orderservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemRequest {
    private String menuItemId;
    private Integer quantity;
}
