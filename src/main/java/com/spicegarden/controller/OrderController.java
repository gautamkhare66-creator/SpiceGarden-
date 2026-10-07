package com.spicegarden.controller;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.repository.MenuItemRepository;
import com.spicegarden.service.OrderService;
import com.spicegarden.service.dto.OrderRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {
    private final MenuItemRepository menuRepository;
    private final OrderService orderService;

    public OrderController(MenuItemRepository menuRepository, OrderService orderService) {
        this.menuRepository = menuRepository;
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public String order(@SessionAttribute(name = "cart", required = false) List<CartItem> cart, ModelMap model) {
        List<CartItem> items = cart == null ? new ArrayList<>() : cart;
        BigDecimal total = items.stream()
                .map(entry -> entry.price().multiply(BigDecimal.valueOf(entry.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("menu", menuRepository.findByAvailableTrueOrderByNameAsc());
        model.addAttribute("cart", items);
        model.addAttribute("total", total);
        return "order";
    }

    @PostMapping("/order/add")
    @SuppressWarnings("unchecked")
    public String addToCart(@RequestParam Long itemId, @RequestParam int quantity, HttpSession session) {
        MenuItem menuItem = menuRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }
        for (int index = 0; index < cart.size(); index++) {
            CartItem entry = cart.get(index);
            if (entry.itemId().equals(itemId)) {
                cart.set(index, new CartItem(
                        itemId, menuItem.getName(), menuItem.getPrice(), entry.quantity() + quantity));
                session.setAttribute("cart", cart);
                return "redirect:/order";
            }
        }
        cart.add(new CartItem(itemId, menuItem.getName(), menuItem.getPrice(), quantity));
        session.setAttribute("cart", cart);
        return "redirect:/order";
    }

    @PostMapping("/orders")
    public String placeOrder(@RequestParam String customerName, @RequestParam String email,
                            @RequestParam String phone, @RequestParam String orderType,
                            @RequestParam String notes,
                            @SessionAttribute(name = "cart", required = false) List<CartItem> cart,
                            HttpSession session, ModelMap model) {
        try {
            if (cart == null || cart.isEmpty()) {
                throw new IllegalArgumentException("Add at least one menu item before placing your order");
            }
            List<OrderRequest.OrderItemRequest> items = cart.stream()
                    .map(entry -> new OrderRequest.OrderItemRequest(entry.itemId(), entry.quantity()))
                    .toList();
            var order = orderService.createOrder(new OrderRequest(
                    customerName, email, phone, orderType, notes, items));
            session.removeAttribute("cart");
            model.addAttribute("order", order);
            return "order-confirmation";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "order";
        }
    }

    public record CartItem(Long itemId, String name, BigDecimal price, int quantity) {
    }
}
