package com.track.moneta.backend.repositories;

import com.track.moneta.backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all top-level categories (where parent is null) for a specific user
//    List<Category> findByUserIdAndParentIsNull(Long userId);

    // Find all categories for a specific user
    List<Category> findByUserId(Long userId);

    // Find a specific category by its ID and user ID to ensure ownership
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories s WHERE c.user.id = :userId AND c.parent IS NULL")
    List<Category> findTopLevelCategoriesWithSubcategories(Long userId);
}