package com.yafd.orderservice.client;

import com.yafd.orderservice.dto.external.MenuItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.menu-url}")
    private String menuServiceUrl;

    public List<MenuItemDto> batchFetchItems(List<String> itemIds) {
        String ids = String.join(",", itemIds);
        String url = menuServiceUrl + "/api/menu-items/batch?ids=" + ids;
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MenuItemDto>>() {}
        ).getBody();
    }
}
