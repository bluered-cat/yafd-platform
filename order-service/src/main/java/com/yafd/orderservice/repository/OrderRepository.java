package com.yafd.orderservice.repository;

import com.yafd.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findByRiderIdOrderByCreatedAtDesc(Long riderId);
}
