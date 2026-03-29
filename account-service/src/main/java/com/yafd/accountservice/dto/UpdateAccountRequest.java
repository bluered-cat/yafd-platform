package com.yafd.accountservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateAccountRequest {
    private String name;
    private String phone;
    private String email;
}
