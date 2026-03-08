package com.yafd.menuservice.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {
    private String id;
    private String menuId;
    private String restaurantId;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean isAvailable;
    private String restaurantName;
    private String createdAt;
    private String updatedAt;
}
