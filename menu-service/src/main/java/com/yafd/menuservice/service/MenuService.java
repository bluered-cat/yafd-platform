package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.MenuRequest;
import com.yafd.menuservice.model.Menu;
import com.yafd.menuservice.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository repository;

    public Menu create(String restaurantId, MenuRequest request) throws ExecutionException, InterruptedException {
        Menu menu = Menu.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();
        return repository.save(menu);
    }

    public Menu getById(String id) throws ExecutionException, InterruptedException {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
    }

    public List<Menu> getByRestaurantId(String restaurantId) throws ExecutionException, InterruptedException {
        return repository.findByRestaurantId(restaurantId);
    }

    public Menu update(String id, MenuRequest request) throws ExecutionException, InterruptedException {
        Menu menu = getById(id);
        if (request.getName() != null) menu.setName(request.getName());
        if (request.getDescription() != null) menu.setDescription(request.getDescription());
        return repository.save(menu);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        repository.deleteById(id);
    }
}
