package com.spicegarden.controller;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.repository.MenuItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final MenuItemRepository repository;

    public HomeController(MenuItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String home(ModelMap model) {
        List<MenuItem> menu = repository.findByAvailableTrueOrderByNameAsc();
        model.addAttribute("menu", menu);
        model.addAttribute("categories", menu.stream().map(MenuItem::getCategory).distinct().toList());
        return "index";
    }
}
