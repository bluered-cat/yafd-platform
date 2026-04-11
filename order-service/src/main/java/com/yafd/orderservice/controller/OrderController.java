package com.yafd.orderservice.controller;

import com.yafd.orderservice.dto.OrderResponse;
import com.yafd.orderservice.dto.SubmitOrderRequest;
import com.yafd.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> submitOrder(@RequestBody SubmitOrderRequest request) {
        return ResponseEntity.ok(orderService.submitOrder(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getByUserId(userId));
    }

