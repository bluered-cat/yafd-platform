package com.yafd.accountservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterAccountRequest {
    private String firebaseUid;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String vehicleType;
}
