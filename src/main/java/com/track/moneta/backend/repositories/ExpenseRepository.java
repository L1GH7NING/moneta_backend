package com.track.moneta.backend.repositories;

import com.track.moneta.backend.models.Expense;
import com.track.moneta.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

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

}