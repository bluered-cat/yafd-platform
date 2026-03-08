package com.yafd.menuservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuRequest {
    private String name;
    private String description;
}
