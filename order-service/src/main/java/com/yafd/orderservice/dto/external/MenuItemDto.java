package com.yafd.orderservice.dto.external;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MenuItemDto {
    private String id;
    private String menuId;
    private String restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Boolean isAvailable;
    private String restaurantName;
}
