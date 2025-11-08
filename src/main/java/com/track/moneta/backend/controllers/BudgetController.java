package com.track.moneta.backend.controllers;

import com.track.moneta.backend.dto.BudgetRequestDTO;
import com.track.moneta.backend.dto.BudgetResponseDTO;
import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.services.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @AuthenticationPrincipal UserDTO currentUser,
            @Valid @RequestBody BudgetRequestDTO budgetDTO
    ) {
        Long userId = currentUser.getId(); // Get the ID from the principal
        BudgetResponseDTO createdBudget = budgetService.createBudget(userId, budgetDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<BudgetResponseDTO>> createMultipleBudgets(
            @AuthenticationPrincipal UserDTO currentUser,
            @Valid @RequestBody List<BudgetRequestDTO> budgetDTOs
    ) {
        Long userId = currentUser.getId();
        List<BudgetResponseDTO> createdBudgets = budgetService.createMultipleBudgets(userId, budgetDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudgets);
    }

    @GetMapping("/current-cycle")
    public ResponseEntity<List<BudgetResponseDTO>> getCurrentCycleBudgets(
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = currentUser.getId();
        List<BudgetResponseDTO> budgets = budgetService.getCurrentCycleBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets(
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = currentUser.getId();
        List<BudgetResponseDTO> budgets = budgetService.getAllUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(
            @AuthenticationPrincipal UserDTO currentUser,
            @PathVariable Long id
    ) {
        Long userId = currentUser.getId();
        BudgetResponseDTO budget = budgetService.getBudgetById(userId, id);
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @AuthenticationPrincipal UserDTO currentUser,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDTO budgetDTO) {
        Long userId = currentUser.getId();
        BudgetResponseDTO updatedBudget = budgetService.updateBudget(userId, id, budgetDTO);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal UserDTO currentUser,
            @PathVariable Long id
    ) {
        Long userId = currentUser.getId();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }
}