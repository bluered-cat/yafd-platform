package com.yafd.orderservice.repository;

import com.yafd.orderservice.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByRiderIdOrderByCreatedAtDesc(Long riderId);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(Long id);
}
