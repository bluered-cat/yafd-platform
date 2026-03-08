package com.yafd.menuservice.repository;

import com.google.cloud.firestore.*;
import com.yafd.menuservice.model.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "menus";

    public Menu save(Menu menu) throws ExecutionException, InterruptedException {
        String now = Instant.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("restaurantId", menu.getRestaurantId());
        data.put("name", menu.getName());
        data.put("description", menu.getDescription());
        data.put("isActive", menu.getIsActive() != null ? menu.getIsActive() : true);
        data.put("updatedAt", now);

        if (menu.getId() == null) {
            data.put("createdAt", now);
            DocumentReference docRef = firestore.collection(COLLECTION).document();
            docRef.set(data).get();
            menu.setId(docRef.getId());
            menu.setCreatedAt(now);
        } else {
            firestore.collection(COLLECTION).document(menu.getId()).set(data, SetOptions.merge()).get();
        }
        menu.setUpdatedAt(now);
        return menu;
    }

    public Optional<Menu> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        if (!doc.exists()) return Optional.empty();
        return Optional.of(docToMenu(doc));
    }

    public List<Menu> findByRestaurantId(String restaurantId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                .whereEqualTo("restaurantId", restaurantId).get().get().getDocuments();
        return docs.stream().map(this::docToMenu).toList();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).delete().get();
    }

    private Menu docToMenu(DocumentSnapshot doc) {
        return Menu.builder()
                .id(doc.getId())
                .restaurantId(doc.getString("restaurantId"))
                .name(doc.getString("name"))
                .description(doc.getString("description"))
                .isActive(doc.getBoolean("isActive"))
                .createdAt(doc.getString("createdAt"))
                .updatedAt(doc.getString("updatedAt"))
                .build();
    }
}
