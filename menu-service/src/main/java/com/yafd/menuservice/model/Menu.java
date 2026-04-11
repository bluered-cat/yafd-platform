package com.yafd.menuservice.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {
    private String id;
    private String restaurantId;
    private String name;
    private String description;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
