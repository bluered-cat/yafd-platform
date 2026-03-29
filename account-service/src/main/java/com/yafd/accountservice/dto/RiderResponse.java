package com.yafd.accountservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderResponse {
    private Long id;
    private String name;
    private String phone;
    private String vehicleType;
    private Boolean isAvailable;
}
