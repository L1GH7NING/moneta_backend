package com.track.moneta.backend.services;

import com.track.moneta.backend.dto.BudgetRequestDTO;
import com.track.moneta.backend.dto.BudgetResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface BudgetService {
    BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO budgetDTO);
    BudgetResponseDTO getBudgetById(Long userId, Long budgetId);

    List<BudgetResponseDTO> getAllUserBudgets(Long userId);
    List<BudgetResponseDTO> getCurrentCycleBudgets(Long userId);
//    List<BudgetResponseDTO> getAllBudgets(Long userId, LocalDate periodStart, LocalDate periodEnd);
    BudgetResponseDTO updateBudget(Long userId, Long budgetId, BudgetRequestDTO budgetDTO);
    void deleteBudget(Long userId, Long budgetId);

    List<BudgetResponseDTO> createMultipleBudgets(Long userId, List<BudgetRequestDTO> budgetDTOs);
}