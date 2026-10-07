package com.spicegarden.controller;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.domain.Order;
import com.spicegarden.domain.Reservation;
import com.spicegarden.repository.MenuItemRepository;
import com.spicegarden.service.OrderService;
import com.spicegarden.service.ReservationService;
import com.spicegarden.service.dto.OrderRequest;
import com.spicegarden.service.dto.ReservationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantApiController {
    private final MenuItemRepository menuRepository;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public RestaurantApiController(MenuItemRepository menuRepository, OrderService orderService,
                                   ReservationService reservationService) {
        this.menuRepository = menuRepository;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    @GetMapping("/menu")
    public List<MenuItemResponse> menu() {
        return menuRepository.findByAvailableTrueOrderByNameAsc().stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return OrderResponse.from(orderService.createOrder(request));
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@RequestBody ReservationRequest request) {
        return ReservationResponse.from(reservationService.createReservation(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException exception) {
        return new ApiError(exception.getMessage());
    }

    public record MenuItemResponse(Long id, String name, String description, BigDecimal price,
                                   String category, String imageUrl) {
        private static MenuItemResponse from(MenuItem item) {
            return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(),
                    item.getPrice(), item.getCategory(), item.getImageUrl());
        }
    }

    public record OrderResponse(Long id, String customerName, BigDecimal total, String status) {
        private static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getCustomerName(), order.getTotal(), order.getStatus());
        }
    }

    public record ReservationResponse(Long id, String customerName, String dateTime,
                                      int partySize, String status) {
        private static ReservationResponse from(Reservation reservation) {
            return new ReservationResponse(reservation.getId(), reservation.getCustomerName(),
                    reservation.getDateTime().toString(), reservation.getPartySize(), reservation.getStatus());
        }
    }

    public record ApiError(String message) {
    }
}