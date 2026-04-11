package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.MenuRequest;
import com.yafd.menuservice.model.Menu;
import com.yafd.menuservice.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository repository;

    @InjectMocks
    private MenuService menuService;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void create_shouldSetRestaurantIdAndIsActiveTrue() throws ExecutionException, InterruptedException {
        MenuRequest request = MenuRequest.builder().name("Mains").description("Main dishes").build();
        Menu saved = Menu.builder().id("m1").restaurantId("r1").name("Mains").isActive(true).build();
        when(repository.save(any(Menu.class))).thenReturn(saved);

        Menu result = menuService.create("r1", request);

        assertThat(result.getRestaurantId()).isEqualTo("r1");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getName()).isEqualTo("Mains");
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnMenu_whenFound() throws ExecutionException, InterruptedException {
        Menu menu = Menu.builder().id("m1").restaurantId("r1").name("Mains").build();
        when(repository.findById("m1")).thenReturn(Optional.of(menu));

        Menu result = menuService.getById("m1");

        assertThat(result.getId()).isEqualTo("m1");
        assertThat(result.getName()).isEqualTo("Mains");
    }

    @Test
    void getById_shouldThrowException_whenMenuNotFound() throws ExecutionException, InterruptedException {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getById("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu not found");
    }

    // -------------------------------------------------------------------------
    // getByRestaurantId()
    // -------------------------------------------------------------------------

    @Test
    void getByRestaurantId_shouldReturnAllMenusForRestaurant() throws ExecutionException, InterruptedException {
        List<Menu> menus = List.of(
                Menu.builder().id("m1").restaurantId("r1").name("Mains").build(),
                Menu.builder().id("m2").restaurantId("r1").name("Drinks").build(),
                Menu.builder().id("m3").restaurantId("r1").name("Sides").build()
        );
        when(repository.findByRestaurantId("r1")).thenReturn(menus);

        List<Menu> result = menuService.getByRestaurantId("r1");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Menu::getName).containsExactly("Mains", "Drinks", "Sides");
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void update_shouldUpdateNameAndDescription_whenProvided() throws ExecutionException, InterruptedException {
        Menu existing = Menu.builder().id("m1").restaurantId("r1").name("Old Name").description("Old desc").build();
        when(repository.findById("m1")).thenReturn(Optional.of(existing));

        Menu updated = Menu.builder().id("m1").restaurantId("r1").name("New Name").description("New desc").build();
        when(repository.save(any(Menu.class))).thenReturn(updated);

        MenuRequest request = MenuRequest.builder().name("New Name").description("New desc").build();
        Menu result = menuService.update("m1", request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New desc");
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldCallRepositoryDeleteById() throws ExecutionException, InterruptedException {
        menuService.delete("m1");

        verify(repository).deleteById("m1");
    }
}
