package com.yafd.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemResponse {
    private Long id;
    private String menuItemId;
    private String menuItemName;
    private String restaurantName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
