package com.yafd.orderservice.dto.external;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddressDto {
    private Long id;
    private String label;
    private String street;
    private String unitNumber;
    private String city;
    private String postalCode;
    private boolean isDefault;
}
