package com.track.moneta.backend.services;

import com.track.moneta.backend.dto.DailyExpenseDTO;
import com.track.moneta.backend.dto.ExpenseFilterDTO;
import com.track.moneta.backend.dto.ExpenseRequestDTO;
import com.track.moneta.backend.dto.ExpenseResponseDTO;
import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.payload.CategoryExpense;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {

    // Changed method signature to accept Long userId
    ExpenseResponseDTO createExpense(Long userId, ExpenseRequestDTO request);

    List<ExpenseResponseDTO> getAllExpenses(Long userId, ExpenseFilterDTO filter);

    ExpenseResponseDTO getExpenseById(Long id, Long userId);

    ExpenseResponseDTO updateExpense(Long id, Long userId, ExpenseRequestDTO request);

    void deleteExpense(Long id, Long userId);

    List<ExpenseResponseDTO> getExpensesByDateFilter(Long userId, Integer year, Integer month, Integer day);

    Double getTotalExpensesInRange(Long userId, LocalDate startDate, LocalDate endDate);


    List<CategoryExpense> getTotalExpensesInRangeCategoryWise(Long userId, LocalDate startDate, LocalDate endDate);

    List<DailyExpenseDTO> getTotalExpensesGroupedByDay(Long userId, LocalDate startDate, LocalDate endDate);
}