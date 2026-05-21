package com.yafd.orderservice.service;

import com.yafd.orderservice.client.*;
import com.yafd.orderservice.dto.*;
import com.yafd.orderservice.dto.external.*;
import com.yafd.orderservice.entity.Order;
import com.yafd.orderservice.entity.OrderItem;
import com.yafd.orderservice.enums.OrderStatus;
import com.yafd.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuServiceClient menuServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final VoucherServiceClient voucherServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Transactional
    public OrderResponse submitOrder(SubmitOrderRequest request) {
        log.info("Submitting order for user: {}", request.getUserId());

        // 1. Fetch item prices from Menu Service
        List<String> itemIds = request.getItems().stream()
                .map(OrderItemRequest::getMenuItemId)
                .collect(Collectors.toList());
        List<MenuItemDto> menuItems = menuServiceClient.batchFetchItems(itemIds);

        // Validate all items exist and are available
        Map<String, MenuItemDto> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItemDto::getId, item -> item));
        for (OrderItemRequest item : request.getItems()) {
            MenuItemDto menuItem = menuItemMap.get(item.getMenuItemId());
            if (menuItem == null) {
                throw new RuntimeException("Menu item not found: " + item.getMenuItemId());
            }
            if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                throw new RuntimeException("Menu item not available: " + menuItem.getName());
            }
        }

        // 2. Calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            MenuItemDto menuItem = menuItemMap.get(item.getMenuItemId());
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        // 3. Validate voucher if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            ValidateVoucherResponse voucherValidation = voucherServiceClient.validateVoucher(
                    request.getVoucherCode(), subtotal);
            if (!voucherValidation.isValid()) {
                throw new RuntimeException("Voucher invalid: " + voucherValidation.getMessage());
            }
            discountAmount = voucherValidation.getDiscountAmount();
        }

        // 4. Calculate total
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 5. Fetch delivery address from Account Service
        AddressDto address = accountServiceClient.getAddress(request.getUserId(), request.getAddressId());
        String deliveryAddress = formatAddress(address);

        // 6. Create order record with PENDING status
        Order order = Order.builder()
                .userId(request.getUserId())
                .deliveryAddress(deliveryAddress)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .voucherCode(request.getVoucherCode())
                .paymentMethodId(request.getPaymentMethodId())
                .status(OrderStatus.PENDING)
                .build();

        // Build order items
        List<OrderItem> orderItems = request.getItems().stream().map(item -> {
            MenuItemDto menuItem = menuItemMap.get(item.getMenuItemId());
            return OrderItem.builder()
                    .order(order)
                    .menuItemId(item.getMenuItemId())
                    .menuItemName(menuItem.getName())
                    .restaurantName(menuItem.getRestaurantName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }).collect(Collectors.toList());
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // 7. Redeem voucher if provided
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            try {
                RedeemVoucherResponse redeemResponse = voucherServiceClient.redeemVoucher(
                        request.getVoucherCode(), savedOrder.getId());
                if (!redeemResponse.isSuccess()) {
                    savedOrder.setStatus(OrderStatus.FAILED);
                    orderRepository.save(savedOrder);
                    throw new RuntimeException("Voucher redemption failed: " + redeemResponse.getMessage());
                }
            } catch (RuntimeException e) {
                savedOrder.setStatus(OrderStatus.FAILED);
                orderRepository.save(savedOrder);
                throw e;
            }
        }

        // 8. Process payment
        try {
            PaymentResponse paymentResponse = paymentServiceClient.processPayment(
                    savedOrder.getId(), totalAmount, request.getPaymentMethodId());
            savedOrder.setPaymentId(paymentResponse.getId());

            if ("COMPLETED".equals(paymentResponse.getStatus())) {
                savedOrder.setStatus(OrderStatus.CONFIRMED);

                // 9. Auto-assign rider
                try {
                    RiderDto rider = accountServiceClient.getAvailableRider();
                    if (rider != null) {
                        savedOrder.setRiderId(rider.getId());
                        accountServiceClient.updateRiderAvailability(rider.getId(), false);
                        log.info("Assigned rider {} to order {}", rider.getName(), savedOrder.getId());
                    } else {
                        log.warn("No available riders for order {}", savedOrder.getId());
                    }
                } catch (Exception e) {
                    log.warn("Failed to assign rider for order {}: {}", savedOrder.getId(), e.getMessage());
                }
            } else {
                savedOrder.setStatus(OrderStatus.FAILED);
            }
        } catch (Exception e) {
            savedOrder.setStatus(OrderStatus.FAILED);
            log.error("Payment processing failed for order {}: {}", savedOrder.getId(), e.getMessage());
        }

        savedOrder = orderRepository.save(savedOrder);
        return toResponse(savedOrder);
    }

    public OrderResponse getById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return toResponse(order);
    }

    public List<OrderResponse> getByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getByRiderId(Long riderId) {
        return orderRepository.findByRiderIdOrderByCreatedAtDesc(riderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderStatus status = OrderStatus.valueOf(newStatus);
        order.setStatus(status);

        // If delivered, release the rider
        if (status == OrderStatus.DELIVERED && order.getRiderId() != null) {
            try {
                accountServiceClient.updateRiderAvailability(order.getRiderId(), true);
                log.info("Released rider {} from order {}", order.getRiderId(), orderId);
            } catch (Exception e) {
                log.warn("Failed to release rider for order {}: {}", orderId, e.getMessage());
            }
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteByUserId(String userId) {
        log.info("Deleting all orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        orderRepository.deleteAll(orders);
        log.info("Deleted {} orders for user: {}", orders.size(), userId);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: cannot cancel another user's order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Can only cancel orders with PENDING status");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Release rider if assigned
        if (order.getRiderId() != null) {
            try {
                accountServiceClient.updateRiderAvailability(order.getRiderId(), true);
            } catch (Exception e) {
                log.warn("Failed to release rider on cancel for order {}: {}", orderId, e.getMessage());
            }
        }

        return toResponse(orderRepository.save(order));
    }

    private String formatAddress(AddressDto address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getStreet());
        if (address.getUnitNumber() != null && !address.getUnitNumber().isBlank()) {
            sb.append(", ").append(address.getUnitNumber());
        }
        sb.append(", ").append(address.getCity());
        sb.append(" ").append(address.getPostalCode());
        return sb.toString();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .restaurantName(item.getRestaurantName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .deliveryAddress(order.getDeliveryAddress())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .voucherCode(order.getVoucherCode())
                .paymentMethodId(order.getPaymentMethodId())
                .status(order.getStatus().name())
                .paymentId(order.getPaymentId())
                .riderId(order.getRiderId())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
