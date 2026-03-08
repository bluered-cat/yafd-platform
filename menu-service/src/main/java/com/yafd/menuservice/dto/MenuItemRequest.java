package com.yafd.menuservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItemRequest {
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
}
