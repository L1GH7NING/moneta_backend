package com.track.moneta.backend.repositories;

import com.track.moneta.backend.models.Expense;
import com.track.moneta.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUser(User user, Pageable pageable);

    Optional<Expense> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);

    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate);


    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate")
    Optional<Double> getTotalExpenseByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category.name, SUM(e.amount) " +
            "FROM Expense e " +
            "WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category.name ")
    List<Object[]> getTotalExpensesGroupedByCategory(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.expenseDate, SUM(e.amount) " +
            "FROM Expense e " +
            "WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.expenseDate " +
            "ORDER BY e.expenseDate ASC")
    List<Object[]> getTotalExpensesGroupedByDay(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Date range filters
    Page<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateGreaterThanEqual(User user, LocalDate startDate, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateLessThanEqual(User user, LocalDate endDate, Pageable pageable);

    // Category filter
    Page<Expense> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    // Combined filters - Date range + Category
    Page<Expense> findByUserAndExpenseDateBetweenAndCategoryId(User user, LocalDate startDate, LocalDate endDate, Long categoryId, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateGreaterThanEqualAndCategoryId(User user, LocalDate startDate, Long categoryId, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateLessThanEqualAndCategoryId(User user, LocalDate endDate, Long categoryId, Pageable pageable);

}