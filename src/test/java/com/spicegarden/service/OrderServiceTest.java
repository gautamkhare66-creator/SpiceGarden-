package com.spicegarden.service;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.domain.Order;
import com.spicegarden.domain.OrderItem;
import com.spicegarden.repository.MenuItemRepository;
import com.spicegarden.repository.OrderRepository;
import com.spicegarden.service.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private MenuItemRepository menuRepository;
    private OrderRepository orderRepository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        menuRepository = Mockito.mock(MenuItemRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        service = new OrderService(menuRepository, orderRepository);
    }

    @Test
    void createsOrderUsingCurrentMenuPrices() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Paneer Tikka");
        item.setPrice(new BigDecimal("12.50"));
        item.setAvailable(true);

        Mockito.when(menuRepository.findAllById(List.of(1L))).thenReturn(List.of(item));
        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        OrderRequest request = new OrderRequest(
                "Asha Patel", "asha@example.com", "9876543210", "delivery", "No onions",
                List.of(new OrderRequest.OrderItemRequest(1L, 2))
        );

        Order created = service.createOrder(request);

        assertEquals(25.00, created.getTotal().doubleValue());
        assertEquals(2, created.getItems().get(0).getQuantity());
        assertEquals("Paneer Tikka", created.getItems().get(0).getName());
        assertEquals(42L, created.getId());
    }

    @Test
    void rejectsUnavailableMenuItem() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Paneer Tikka");
        item.setPrice(new BigDecimal("12.50"));
        item.setAvailable(false);

        Mockito.when(menuRepository.findAllById(List.of(1L))).thenReturn(List.of(item));

        OrderRequest request = new OrderRequest(
                "Asha Patel", "asha@example.com", "9876543210", "delivery", "",
                List.of(new OrderRequest.OrderItemRequest(1L, 1))
        );

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(request));
    }
}
