package com.yafd.menuservice.controller;

import com.yafd.menuservice.dto.RestaurantRequest;
import com.yafd.menuservice.model.Restaurant;
import com.yafd.menuservice.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAll(@RequestParam(required = false) String cuisine)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getAll(cuisine));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> search(@RequestParam String q)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.search(q));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> getById(@PathVariable String restaurantId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getById(restaurantId));
    }

    @PostMapping
    public ResponseEntity<Restaurant> create(@RequestBody RestaurantRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> update(@PathVariable String restaurantId,
                                              @RequestBody RestaurantRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.update(restaurantId, request));
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> delete(@PathVariable String restaurantId)
            throws ExecutionException, InterruptedException {
        service.delete(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
