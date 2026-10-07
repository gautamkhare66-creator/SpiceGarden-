package com.spicegarden.repository;

import com.spicegarden.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findAllById(Iterable<Long> ids);
    List<MenuItem> findByCategory(String category);
    List<MenuItem> findByAvailableTrueOrderByNameAsc();
}
