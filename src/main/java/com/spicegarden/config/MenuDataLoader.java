package com.spicegarden.config;

import com.spicegarden.domain.MenuItem;
import com.spicegarden.repository.MenuItemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MenuDataLoader {
    private final MenuItemRepository repository;

    public MenuDataLoader(MenuItemRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void seed() {
        List<MenuItem> seededItems = List.of(
                item("Paneer Tikka", "Charred cottage cheese with peppers and a smoky spice marinade", BigDecimal.valueOf(12.50), "Starter", "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=800&q=80"),
                item("Tandoori Chicken", "Juicy chicken marinated in yogurt, ginger, garlic and aromatic spices", BigDecimal.valueOf(15.00), "Starter", "https://images.unsplash.com/photo-1628294896516-344152572ee8?auto=format&fit=crop&w=800&q=80"),
                item("Spice Garden Biryani", "Fragrant basmati rice with slow-cooked vegetables and saffron", BigDecimal.valueOf(16.50), "Main Course", "https://images.unsplash.com/photo-1563379926898-05f4575a45d8?auto=format&fit=crop&w=800&q=80"),
                item("Butter Chicken", "Tender chicken in a rich tomato, butter and cashew sauce", BigDecimal.valueOf(18.00), "Main Course", "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=800&q=80"),
                item("Treble Paneer Pizza", "Warm toasted base with paneer, peppers, olives and mint", BigDecimal.valueOf(14.00), "Pizza", "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=800&q=80"),
                item("Mango Lassi", "Cool yogurt drink with mango, cardamom and a hint of sweetness", BigDecimal.valueOf(5.50), "Drinks", "https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=800&q=80")
        );
        if (repository.count() == 0) {
            repository.saveAll(seededItems);
            return;
        }

        Map<String, String> repairedImages = Map.of(
                "https://images.unsplash.com/photo-1567188040756-5a5e5f1f6a4b?auto=format&fit=crop&w=800&q=80",
                seededItems.get(0).getImageUrl(),
                "https://images.unsplash.com/photo-1601050690599-df0568f70950?auto=format&fit=crop&w=800&q=80",
                seededItems.get(1).getImageUrl(),
                "https://images.unsplash.com/photo-1626201320195-4e1f8d5c4f3c?auto=format&fit=crop&w=800&q=80",
                seededItems.get(5).getImageUrl());
        repository.findAll().forEach(existingItem -> {
            String correctedImage = repairedImages.get(existingItem.getImageUrl());
            if (correctedImage != null) {
                existingItem.setImageUrl(correctedImage);
                repository.save(existingItem);
            }
        });
    }

    private MenuItem item(String name, String description, BigDecimal price, String category, String imageUrl) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        item.setAvailable(true);
        return item;
    }
}
