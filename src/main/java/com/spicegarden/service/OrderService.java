package com.spicegarden.service;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.domain.Order;
import com.spicegarden.domain.OrderItem;
import com.spicegarden.repository.MenuItemRepository;
import com.spicegarden.repository.OrderRepository;
import com.spicegarden.service.dto.OrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final MenuItemRepository menuRepository;
    private final OrderRepository orderRepository;

    public OrderService(MenuItemRepository menuRepository, OrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one menu item is required");
        }

        List<Long> ids = request.items().stream()
                .map(OrderRequest.OrderItemRequest::itemId)
                .distinct()
                .toList();
        Map<Long, MenuItem> items = menuRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(MenuItem::getId, item -> item));

        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setPhone(request.phone());
        order.setOrderType(request.orderType());
        order.setNotes(request.notes());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("CONFIRMED");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest orderedItem : request.items()) {
            MenuItem menuItem = items.get(orderedItem.itemId());
            if (menuItem == null) {
                throw new IllegalArgumentException("Menu item not found: " + orderedItem.itemId());
            }
            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException("Item is unavailable: " + menuItem.getName());
            }
            if (orderedItem.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setName(menuItem.getName());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setQuantity(orderedItem.quantity());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(orderedItem.quantity())));
        }
        order.setTotal(total);
        return orderRepository.save(order);
    }
}
