package com.yafd.accountservice.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {
    private Long id;
    private String firebaseUid;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String vehicleType;
    private Boolean isAvailable;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
