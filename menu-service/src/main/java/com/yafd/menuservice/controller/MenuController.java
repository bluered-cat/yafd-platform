package com.yafd.menuservice.controller;

import com.yafd.menuservice.dto.MenuRequest;
import com.yafd.menuservice.model.Menu;
import com.yafd.menuservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService service;

    @GetMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<List<Menu>> getByRestaurant(@PathVariable String restaurantId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getByRestaurantId(restaurantId));
    }

    @GetMapping("/api/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<Menu> getById(@PathVariable String restaurantId,
                                        @PathVariable String menuId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getById(menuId));
    }

    @PostMapping("/api/restaurants/{restaurantId}/menus")
    public ResponseEntity<Menu> create(@PathVariable String restaurantId,
                                       @RequestBody MenuRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.create(restaurantId, request));
    }

    @PutMapping("/api/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<Menu> update(@PathVariable String restaurantId,
                                       @PathVariable String menuId,
                                       @RequestBody MenuRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.update(menuId, request));
    }

    @DeleteMapping("/api/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable String restaurantId,
                                       @PathVariable String menuId)
            throws ExecutionException, InterruptedException {
        service.delete(menuId);
        return ResponseEntity.noContent().build();
    }
}
