package com.yafd.menuservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantRequest {
    private String name;
    private String cuisine;
    private String description;
    private String imageUrl;
    private Double rating;
}
