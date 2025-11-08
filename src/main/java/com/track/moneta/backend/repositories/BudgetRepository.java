package com.track.moneta.backend.repositories;

import com.track.moneta.backend.models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Fetch all budgets for a specific user
    List<Budget> findByUserId(Long userId);

    // Fetch a single budget, ensuring it belongs to the user
    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND b.startDate <= :periodEnd " +
            "AND b.endDate >= :periodStart")
    List<Budget> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

//    @Query("SELECT COUNT(b) > 0 FROM Budget b WHERE b.user.id = :userId " +
//            "AND b.category.id = :categoryId " +
//            "AND b.startDate <= :periodEnd " +
//            "AND b.endDate >= :periodStart")
//    boolean existsByUserIdAndCategoryAndDateOverlap(
//            @Param("userId") Long userId,
//            @Param("categoryId") Long categoryId,
//            @Param("periodStart") LocalDate periodStart,
//            @Param("periodEnd") LocalDate periodEnd);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.endDate >= :checkDate")
    List<Budget> findActiveBudgetsByUserId(@Param("userId") Long userId, @Param("checkDate") LocalDate checkDate);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND b.category.id = :categoryId " +
            "AND b.startDate <= :periodEnd " +
            "AND b.endDate >= :periodStart")
    Optional<Budget> findByUserIdAndCategoryAndDateOverlap(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

    @Modifying
    @Query("UPDATE Budget b SET b.startDate = :newStart, b.endDate = :newEnd WHERE b.user.id = :userId AND b.endDate >= :checkDate")
    int updateActiveBudgetDates(
            @Param("userId") Long userId,
            @Param("newStart") LocalDate newStart,
            @Param("newEnd") LocalDate newEnd,
            @Param("checkDate") LocalDate checkDate
    );

}