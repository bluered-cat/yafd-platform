package com.yafd.accountservice.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressRequest {
    private String label;
    private String street;
    private String unitNumber;
    private String city;
    private String postalCode;
    private Boolean isDefault;
}
