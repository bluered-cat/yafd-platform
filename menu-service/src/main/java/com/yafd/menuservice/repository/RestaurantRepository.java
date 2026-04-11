package com.yafd.menuservice.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.yafd.menuservice.model.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class RestaurantRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "restaurants";

    public Restaurant save(Restaurant restaurant) throws ExecutionException, InterruptedException {
        String now = Instant.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("name", restaurant.getName());
        data.put("cuisine", restaurant.getCuisine());
        data.put("description", restaurant.getDescription());
        data.put("imageUrl", restaurant.getImageUrl());
        data.put("rating", restaurant.getRating());
        data.put("isActive", restaurant.getIsActive() != null ? restaurant.getIsActive() : true);
        data.put("updatedAt", now);

        if (restaurant.getId() == null) {
            data.put("createdAt", now);
            DocumentReference docRef = firestore.collection(COLLECTION).document();
            docRef.set(data).get();
            restaurant.setId(docRef.getId());
            restaurant.setCreatedAt(now);
        } else {
            firestore.collection(COLLECTION).document(restaurant.getId()).set(data, SetOptions.merge()).get();
        }
        restaurant.setUpdatedAt(now);
        return restaurant;
    }

    public Optional<Restaurant> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        if (!doc.exists()) return Optional.empty();
        return Optional.of(docToRestaurant(doc));
    }

    public List<Restaurant> findAll() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION).get().get().getDocuments();
        return docs.stream().map(this::docToRestaurant).toList();
    }

    public List<Restaurant> findByCuisine(String cuisine) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                .whereEqualTo("cuisine", cuisine).get().get().getDocuments();
        return docs.stream().map(this::docToRestaurant).toList();
    }

    public List<Restaurant> search(String query) throws ExecutionException, InterruptedException {
        // Firestore doesn't support full-text search natively, so we do client-side filtering
        List<Restaurant> all = findAll();
        String lowerQuery = query.toLowerCase();
        return all.stream()
                .filter(r -> (r.getName() != null && r.getName().toLowerCase().contains(lowerQuery))
                        || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(lowerQuery)))
                .toList();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).delete().get();
    }

    private Restaurant docToRestaurant(DocumentSnapshot doc) {
        return Restaurant.builder()
                .id(doc.getId())
                .name(doc.getString("name"))
                .cuisine(doc.getString("cuisine"))
                .description(doc.getString("description"))
                .imageUrl(doc.getString("imageUrl"))
                .rating(doc.getDouble("rating"))
                .isActive(doc.getBoolean("isActive"))
                .createdAt(doc.getString("createdAt"))
                .updatedAt(doc.getString("updatedAt"))
                .build();
    }
}
