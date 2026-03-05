package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.MenuItemRequest;
import com.yafd.menuservice.model.MenuItem;
import com.yafd.menuservice.model.Restaurant;
import com.yafd.menuservice.repository.MenuItemRepository;
import com.yafd.menuservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository repository;
    private final RestaurantRepository restaurantRepository;

    public MenuItem create(String restaurantId, String menuId, MenuItemRequest request)
            throws ExecutionException, InterruptedException {
        // Fetch restaurant name for denormalization
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        MenuItem item = MenuItem.builder()
                .menuId(menuId)
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .restaurantName(restaurant.getName())
                .build();
        return repository.save(item);
    }

    public MenuItem getById(String id) throws ExecutionException, InterruptedException {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));
    }

    public List<MenuItem> getByMenuId(String menuId) throws ExecutionException, InterruptedException {
        return repository.findByMenuId(menuId);
    }

    public List<MenuItem> batchFetch(List<String> ids) throws ExecutionException, InterruptedException {
        return repository.findByIds(ids);
    }

    public MenuItem update(String id, MenuItemRequest request) throws ExecutionException, InterruptedException {
        MenuItem item = getById(id);
        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        return repository.save(item);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        repository.deleteById(id);
    }
}
