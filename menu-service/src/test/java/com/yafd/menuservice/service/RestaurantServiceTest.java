package com.yafd.menuservice.service;

import com.yafd.menuservice.dto.RestaurantRequest;
import com.yafd.menuservice.model.Restaurant;
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
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository repository;

    @InjectMocks
    private RestaurantService service;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void create_shouldDefaultRatingToZero_whenRatingNotProvided() throws ExecutionException, InterruptedException {
        RestaurantRequest request = RestaurantRequest.builder()
                .name("Warung Makan Selera")
                .cuisine("Malay")
                .build();

        Restaurant saved = Restaurant.builder().id("r1").name("Warung Makan Selera").rating(0.0).isActive(true).build();
        when(repository.save(any(Restaurant.class))).thenReturn(saved);

        Restaurant result = service.create(request);

        assertThat(result.getRating()).isEqualTo(0.0);
    }

    @Test
    void create_shouldUseProvidedRating() throws ExecutionException, InterruptedException {
        RestaurantRequest request = RestaurantRequest.builder()
                .name("Nasi Padang House")
                .cuisine("Malay")
                .rating(4.5)
                .build();

        Restaurant saved = Restaurant.builder().id("r2").name("Nasi Padang House").rating(4.5).isActive(true).build();
        when(repository.save(any(Restaurant.class))).thenReturn(saved);

        Restaurant result = service.create(request);

        assertThat(result.getRating()).isEqualTo(4.5);
    }

    @Test
    void create_shouldSetIsActiveToTrue() throws ExecutionException, InterruptedException {
        RestaurantRequest request = RestaurantRequest.builder().name("Test").cuisine("Chinese").build();
        Restaurant saved = Restaurant.builder().id("r3").isActive(true).build();
        when(repository.save(any(Restaurant.class))).thenReturn(saved);

        Restaurant result = service.create(request);

        assertThat(result.getIsActive()).isTrue();
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnRestaurant_whenFound() throws ExecutionException, InterruptedException {
        Restaurant restaurant = Restaurant.builder().id("r1").name("Warung Makan Selera").build();
        when(repository.findById("r1")).thenReturn(Optional.of(restaurant));

        Restaurant result = service.getById("r1");

        assertThat(result.getId()).isEqualTo("r1");
        assertThat(result.getName()).isEqualTo("Warung Makan Selera");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() throws ExecutionException, InterruptedException {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurant not found");
    }

    // -------------------------------------------------------------------------
    // getAll()
    // -------------------------------------------------------------------------

    @Test
    void getAll_shouldReturnAllRestaurants_whenNoCuisineFilter() throws ExecutionException, InterruptedException {
        List<Restaurant> allRestaurants = List.of(
                Restaurant.builder().id("r1").name("Warung Makan Selera").cuisine("Malay").build(),
                Restaurant.builder().id("r2").name("Dragon Palace").cuisine("Chinese").build()
        );
        when(repository.findAll()).thenReturn(allRestaurants);

        List<Restaurant> result = service.getAll(null);

        assertThat(result).hasSize(2);
        verify(repository).findAll();
        verify(repository, never()).findByCuisine(any());
    }

    @Test
    void getAll_shouldFilterByCuisine_whenCuisineProvided() throws ExecutionException, InterruptedException {
        List<Restaurant> malayRestaurants = List.of(
                Restaurant.builder().id("r1").name("Warung Makan Selera").cuisine("Malay").build()
        );
        when(repository.findByCuisine("Malay")).thenReturn(malayRestaurants);

        List<Restaurant> result = service.getAll("Malay");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCuisine()).isEqualTo("Malay");
        verify(repository).findByCuisine("Malay");
        verify(repository, never()).findAll();
    }

    // -------------------------------------------------------------------------
    // search()
    // -------------------------------------------------------------------------

    @Test
    void search_shouldReturnMatchingRestaurants() throws ExecutionException, InterruptedException {
        List<Restaurant> matches = List.of(
                Restaurant.builder().id("r1").name("Warung Makan Selera").cuisine("Malay").build()
        );
        when(repository.search("malay")).thenReturn(matches);

        List<Restaurant> result = service.search("malay");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Warung Makan Selera");
    }

    @Test
    void search_shouldReturnEmptyList_whenNoMatches() throws ExecutionException, InterruptedException {
        when(repository.search("pizza")).thenReturn(List.of());

        List<Restaurant> result = service.search("pizza");

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldCallRepositoryDeleteById() throws ExecutionException, InterruptedException {
        service.delete("r1");

        verify(repository).deleteById("r1");
    }
}
