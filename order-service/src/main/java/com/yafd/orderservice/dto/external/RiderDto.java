package com.yafd.orderservice.dto.external;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RiderDto {
    private Long id;
    private String name;
    private String phone;
    private String vehicleType;
    private boolean isAvailable;
}
