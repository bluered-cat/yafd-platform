package com.yafd.menuservice.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurant {
    private String id;
    private String name;
    private String cuisine;
    private String description;
    private String imageUrl;
    private Double rating;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
