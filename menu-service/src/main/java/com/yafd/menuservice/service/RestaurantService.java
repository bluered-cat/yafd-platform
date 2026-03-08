package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.RestaurantRequest;
import com.yafd.menuservice.model.Restaurant;
import com.yafd.menuservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository repository;

    public Restaurant create(RestaurantRequest request) throws ExecutionException, InterruptedException {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .cuisine(request.getCuisine())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .rating(request.getRating() != null ? request.getRating() : 0.0)
                .isActive(true)
                .build();
        return repository.save(restaurant);
    }

    public Restaurant getById(String id) throws ExecutionException, InterruptedException {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
    }

    public List<Restaurant> getAll(String cuisine) throws ExecutionException, InterruptedException {
        if (cuisine != null && !cuisine.isBlank()) {
            return repository.findByCuisine(cuisine);
        }
        return repository.findAll();
    }

    public List<Restaurant> search(String query) throws ExecutionException, InterruptedException {
        return repository.search(query);
    }

    public Restaurant update(String id, RestaurantRequest request) throws ExecutionException, InterruptedException {
        Restaurant restaurant = getById(id);
        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getCuisine() != null) restaurant.setCuisine(request.getCuisine());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getImageUrl() != null) restaurant.setImageUrl(request.getImageUrl());
        if (request.getRating() != null) restaurant.setRating(request.getRating());
        return repository.save(restaurant);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        repository.deleteById(id);
    }
}
