package com.yafd.menuservice.controller;

import com.yafd.menuservice.dto.MenuItemRequest;
import com.yafd.menuservice.model.MenuItem;
import com.yafd.menuservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService service;

    @GetMapping("/api/restaurants/{restaurantId}/menus/{menuId}/items")
    public ResponseEntity<List<MenuItem>> getByMenu(@PathVariable String restaurantId,
                                                     @PathVariable String menuId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getByMenuId(menuId));
    }

    @GetMapping("/api/restaurants/{restaurantId}/menus/{menuId}/items/{itemId}")
    public ResponseEntity<MenuItem> getById(@PathVariable String restaurantId,
                                            @PathVariable String menuId,
                                            @PathVariable String itemId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.getById(itemId));
    }

    @PostMapping("/api/restaurants/{restaurantId}/menus/{menuId}/items")
    public ResponseEntity<MenuItem> create(@PathVariable String restaurantId,
                                           @PathVariable String menuId,
                                           @RequestBody MenuItemRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.create(restaurantId, menuId, request));
    }

    @PutMapping("/api/restaurants/{restaurantId}/menus/{menuId}/items/{itemId}")
    public ResponseEntity<MenuItem> update(@PathVariable String restaurantId,
                                           @PathVariable String menuId,
                                           @PathVariable String itemId,
                                           @RequestBody MenuItemRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.update(itemId, request));
    }

    @DeleteMapping("/api/restaurants/{restaurantId}/menus/{menuId}/items/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable String restaurantId,
                                       @PathVariable String menuId,
                                       @PathVariable String itemId)
            throws ExecutionException, InterruptedException {
        service.delete(itemId);
        return ResponseEntity.noContent().build();
    }

    // Batch endpoint for Order Service
    @GetMapping("/api/menu-items/batch")
    public ResponseEntity<List<MenuItem>> batchFetch(@RequestParam String ids)
            throws ExecutionException, InterruptedException {
        List<String> idList = Arrays.asList(ids.split(","));
        return ResponseEntity.ok(service.batchFetch(idList));
    }
}
