package com.yafd.orderservice.service;

import com.yafd.orderservice.client.AccountServiceClient;
import com.yafd.orderservice.client.MenuServiceClient;
import com.yafd.orderservice.client.PaymentServiceClient;
import com.yafd.orderservice.client.VoucherServiceClient;
import com.yafd.orderservice.dto.OrderResponse;
import com.yafd.orderservice.entity.Order;
import com.yafd.orderservice.enums.OrderStatus;
import com.yafd.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuServiceClient menuServiceClient;
    @Mock private AccountServiceClient accountServiceClient;
    @Mock private VoucherServiceClient voucherServiceClient;
    @Mock private PaymentServiceClient paymentServiceClient;

    @InjectMocks
    private OrderService orderService;

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnOrderResponse_whenOrderExists() {
        Order order = buildOrder(1L, "user-01", OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo("user-01");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void getById_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // -------------------------------------------------------------------------
    // getByUserId()
    // -------------------------------------------------------------------------

    @Test
    void getByUserId_shouldReturnAllOrdersForUser() {
        List<Order> orders = List.of(
                buildOrder(1L, "user-01", OrderStatus.CONFIRMED),
                buildOrder(2L, "user-01", OrderStatus.DELIVERED)
        );
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-01")).thenReturn(orders);

        List<OrderResponse> result = orderService.getByUserId("user-01");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponse::getUserId).containsOnly("user-01");
    }

    // -------------------------------------------------------------------------
    // cancelOrder()
    // -------------------------------------------------------------------------

    @Test
    void cancelOrder_shouldCancelPendingOrder() {
        Order order = buildOrder(1L, "user-01", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, "user-01");

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelOrder_shouldThrowException_whenUserIsNotOrderOwner() {
        Order order = buildOrder(1L, "user-01", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "user-02"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderIsNotPending() {
        Order order = buildOrder(1L, "user-01", OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "user-01"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PENDING");
    }

    // -------------------------------------------------------------------------
    // updateStatus()
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_shouldUpdateOrderStatus() {
        Order order = buildOrder(1L, "user-01", OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateStatus(1L, "OUT_FOR_DELIVERY");

        assertThat(response.getStatus()).isEqualTo("OUT_FOR_DELIVERY");
    }

    @Test
    void updateStatus_shouldReleaseRider_whenStatusIsDelivered() {
        Order order = buildOrder(1L, "user-01", OrderStatus.OUT_FOR_DELIVERY);
        order.setRiderId(10L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateStatus(1L, "DELIVERED");

        verify(accountServiceClient).updateRiderAvailability(10L, true);
    }

    @Test
    void updateStatus_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, "DELIVERED"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Order buildOrder(Long id, String userId, OrderStatus status) {
        Order order = Order.builder()
                .id(id)
                .userId(userId)
                .deliveryAddress("123 Test Street, Singapore 123456")
                .subtotal(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("10.00"))
                .status(status)
                .items(new ArrayList<>())
                .build();
        return order;
    }
}
