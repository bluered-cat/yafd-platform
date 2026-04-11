package com.yafd.orderservice.client;

import com.yafd.orderservice.dto.external.AddressDto;
import com.yafd.orderservice.dto.external.RiderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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

    public RiderDto getAvailableRider() {
        String url = accountServiceUrl + "/api/riders/available";
        return restTemplate.getForObject(url, RiderDto.class);
    }

    public void updateRiderAvailability(Long riderId, boolean available) {
        String url = accountServiceUrl + "/api/riders/" + riderId + "/availability";
        restTemplate.put(url, Map.of("available", available));
    }
}
