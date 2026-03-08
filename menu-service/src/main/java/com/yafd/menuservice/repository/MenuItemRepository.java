package com.yafd.menuservice.repository;

import com.google.cloud.firestore.*;
import com.yafd.menuservice.model.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class MenuItemRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "menu_items";

    public MenuItem save(MenuItem item) throws ExecutionException, InterruptedException {
        String now = Instant.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("menuId", item.getMenuId());
        data.put("restaurantId", item.getRestaurantId());
        data.put("name", item.getName());
        data.put("description", item.getDescription());
        data.put("price", item.getPrice());
        data.put("category", item.getCategory());
        data.put("imageUrl", item.getImageUrl());
        data.put("isAvailable", item.getIsAvailable() != null ? item.getIsAvailable() : true);
        data.put("restaurantName", item.getRestaurantName());
        data.put("updatedAt", now);

        if (item.getId() == null) {
            data.put("createdAt", now);
            DocumentReference docRef = firestore.collection(COLLECTION).document();
            docRef.set(data).get();
            item.setId(docRef.getId());
            item.setCreatedAt(now);
        } else {
            firestore.collection(COLLECTION).document(item.getId()).set(data, SetOptions.merge()).get();
        }
        item.setUpdatedAt(now);
        return item;
    }

    public Optional<MenuItem> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        if (!doc.exists()) return Optional.empty();
        return Optional.of(docToMenuItem(doc));
    }

    public List<MenuItem> findByMenuId(String menuId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                .whereEqualTo("menuId", menuId).get().get().getDocuments();
        return docs.stream().map(this::docToMenuItem).toList();
    }

    public List<MenuItem> findByIds(List<String> ids) throws ExecutionException, InterruptedException {
        if (ids.isEmpty()) return Collections.emptyList();
        // Firestore 'in' query supports up to 30 items
        List<MenuItem> results = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += 30) {
            List<String> batch = ids.subList(i, Math.min(i + 30, ids.size()));
            List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                    .whereIn(FieldPath.documentId(), batch).get().get().getDocuments();
            docs.stream().map(this::docToMenuItem).forEach(results::add);
        }
        return results;
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).delete().get();
    }

    private MenuItem docToMenuItem(DocumentSnapshot doc) {
        return MenuItem.builder()
                .id(doc.getId())
                .menuId(doc.getString("menuId"))
                .restaurantId(doc.getString("restaurantId"))
                .name(doc.getString("name"))
                .description(doc.getString("description"))
                .price(doc.getDouble("price"))
                .category(doc.getString("category"))
                .imageUrl(doc.getString("imageUrl"))
                .isAvailable(doc.getBoolean("isAvailable"))
                .restaurantName(doc.getString("restaurantName"))
                .createdAt(doc.getString("createdAt"))
                .updatedAt(doc.getString("updatedAt"))
                .build();
    }
}
