package com.yafd.orderservice.client;

import com.yafd.orderservice.dto.external.AddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.account-url}")
    private String accountServiceUrl;

    public AddressDto getAddress(String userId, Long addressId) {
        String url = accountServiceUrl + "/api/accounts/" + userId + "/addresses/" + addressId;
        return restTemplate.getForObject(url, AddressDto.class);
    }

}

