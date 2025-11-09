package com.track.moneta.backend.repositories;

import com.track.moneta.backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all categories for a specific user
    List<Category> findByUserId(Long userId);

    // Find a specific category by its ID and user ID to ensure ownership
    Optional<Category> findByIdAndUserId(Long id, Long userId);

}