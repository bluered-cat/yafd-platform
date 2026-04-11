package com.yafd.orderservice.service;

import com.yafd.orderservice.client.AccountServiceClient;
import com.yafd.orderservice.client.MenuServiceClient;
import com.yafd.orderservice.dto.OrderItemRequest;
import com.yafd.orderservice.dto.OrderItemResponse;
import com.yafd.orderservice.dto.OrderResponse;
import com.yafd.orderservice.dto.SubmitOrderRequest;
import com.yafd.orderservice.dto.external.AddressDto;
import com.yafd.orderservice.dto.external.MenuItemDto;
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

        // 3. Fetch delivery address from Account Service
        AddressDto address = accountServiceClient.getAddress(request.getUserId(), request.getAddressId());
        String deliveryAddress = formatAddress(address);

        // 4. Build order items and create order with PENDING status
        Order order = Order.builder()
                .userId(request.getUserId())
                .deliveryAddress(deliveryAddress)
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal)
                .voucherCode(request.getVoucherCode())
                .paymentMethodId(request.getPaymentMethodId())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = request.getItems().stream().map(item -> {
            MenuItemDto menuItem = menuItemMap.get(item.getMenuItemId());
            return OrderItem.builder()
                    .order(order)
                    .menuItemId(item.getMenuItemId())
