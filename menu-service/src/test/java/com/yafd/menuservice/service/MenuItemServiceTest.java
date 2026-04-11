package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.MenuItemRequest;
import com.yafd.menuservice.model.MenuItem;
import com.yafd.menuservice.model.Restaurant;
import com.yafd.menuservice.repository.MenuItemRepository;
import com.yafd.menuservice.repository.RestaurantRepository;
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
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository repository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void create_shouldDenormalizeRestaurantName_andSetIsAvailableTrue() throws ExecutionException, InterruptedException {
        Restaurant restaurant = Restaurant.builder().id("r1").name("Warung Makan Selera").build();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(restaurant));

        MenuItem saved = MenuItem.builder()
                .id("i1").menuId("m1").restaurantId("r1")
                .name("Nasi Lemak Ayam").price(7.50)
                .restaurantName("Warung Makan Selera")
                .isAvailable(true)
                .build();
        when(repository.save(any(MenuItem.class))).thenReturn(saved);

        MenuItemRequest request = MenuItemRequest.builder()
                .name("Nasi Lemak Ayam").price(7.50).category("Rice")
                .build();
        MenuItem result = menuItemService.create("r1", "m1", request);

        assertThat(result.getRestaurantName()).isEqualTo("Warung Makan Selera");
        assertThat(result.getIsAvailable()).isTrue();
    }

    @Test
    void create_shouldThrowException_whenRestaurantNotFound() throws ExecutionException, InterruptedException {
        when(restaurantRepository.findById("unknown")).thenReturn(Optional.empty());

        MenuItemRequest request = MenuItemRequest.builder().name("Item").price(5.00).build();

        assertThatThrownBy(() -> menuItemService.create("unknown", "m1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurant not found");
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnMenuItem_whenFound() throws ExecutionException, InterruptedException {
        MenuItem item = MenuItem.builder().id("i1").name("Nasi Lemak Ayam").price(7.50).build();
        when(repository.findById("i1")).thenReturn(Optional.of(item));

        MenuItem result = menuItemService.getById("i1");

        assertThat(result.getId()).isEqualTo("i1");
        assertThat(result.getName()).isEqualTo("Nasi Lemak Ayam");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() throws ExecutionException, InterruptedException {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.getById("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu item not found");
    }

    // -------------------------------------------------------------------------
    // getByMenuId()
    // -------------------------------------------------------------------------

    @Test
    void getByMenuId_shouldReturnAllItemsForMenu() throws ExecutionException, InterruptedException {
        List<MenuItem> items = List.of(
                MenuItem.builder().id("i1").menuId("m1").name("Nasi Lemak Ayam").build(),
                MenuItem.builder().id("i2").menuId("m1").name("Rendang Daging").build()
        );
        when(repository.findByMenuId("m1")).thenReturn(items);

        List<MenuItem> result = menuItemService.getByMenuId("m1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MenuItem::getName)
                .containsExactly("Nasi Lemak Ayam", "Rendang Daging");
    }

    // -------------------------------------------------------------------------
    // batchFetch()
    // -------------------------------------------------------------------------

    @Test
    void batchFetch_shouldReturnItemsForGivenIds() throws ExecutionException, InterruptedException {
        List<String> ids = List.of("i1", "i2");
        List<MenuItem> items = List.of(
                MenuItem.builder().id("i1").name("Nasi Lemak Ayam").build(),
                MenuItem.builder().id("i2").name("Rendang Daging").build()
        );
        when(repository.findByIds(ids)).thenReturn(items);

        List<MenuItem> result = menuItemService.batchFetch(ids);

        assertThat(result).hasSize(2);
        verify(repository).findByIds(ids);
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldCallRepositoryDeleteById() throws ExecutionException, InterruptedException {
        menuItemService.delete("i1");

        verify(repository).deleteById("i1");
    }
}
