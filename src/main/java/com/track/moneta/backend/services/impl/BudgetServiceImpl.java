package com.track.moneta.backend.services.impl;

import com.track.moneta.backend.dto.BudgetRequestDTO;
import com.track.moneta.backend.dto.BudgetResponseDTO;
import com.track.moneta.backend.exceptions.APIException;
import com.track.moneta.backend.models.Budget;
import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.models.User;
import com.track.moneta.backend.repositories.BudgetRepository;
import com.track.moneta.backend.repositories.CategoryRepository;
import com.track.moneta.backend.repositories.UserRepository;
import com.track.moneta.backend.services.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.track.moneta.backend.utility.CommonUtils.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> getCurrentCycleBudgets(Long userId) {
        // 1. Fetch User to get budget start preference
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        LocalDate[] currentPeriod = calculateBudgetPeriod(user, LocalDate.now());
        LocalDate periodStart = currentPeriod[0];
        LocalDate periodEnd = currentPeriod[1];

        // 3. Fetch budgets that overlap with the calculated period
        List<Budget> budgets = budgetRepository.findByUserIdAndDateRange(userId, periodStart, periodEnd);

        return budgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> getAllUserBudgets(Long userId) {
        // Fetch all budgets for the user, no date filtering
        List<Budget> budgets = budgetRepository.findByUserId(userId);

        return budgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetResponseDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO budgetDTO) {
        // 1. Fetch User and Category
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        Category category = categoryRepository.findById(budgetDTO.getCategoryId())
                .orElseThrow(() -> new APIException("Category not found with id: " + budgetDTO.getCategoryId()));

        LocalDate[] period = calculateBudgetPeriod(user, LocalDate.now());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];
        LocalDateTime now = LocalDateTime.now();

        Optional<Budget> existingBudgetOpt = budgetRepository.findByUserIdAndCategoryAndDateOverlap(
                userId,
                budgetDTO.getCategoryId(),
                startDate,
                endDate
        );

        Budget budget;

        if (existingBudgetOpt.isPresent()) {
            // **UPDATE Existing Budget (UPSERT Logic)**
            budget = existingBudgetOpt.get();
            budget.setAmount(budgetDTO.getAmount()); // Update the amount
            // Dates, User, Category remain the same
            budget.setUpdatedAt(now);
        } else {
            // **CREATE New Budget (INSERT Logic)**
            budget = new Budget();
            budget.setAmount(budgetDTO.getAmount());
            budget.setStartDate(startDate); // Set calculated start date
            budget.setEndDate(endDate);     // Set calculated end date
            budget.setCategory(category);
            budget.setUser(user);
            budget.setCreatedAt(now);
            budget.setUpdatedAt(now);
        }

        // 4. Save (or update) and return DTO
        Budget savedBudget = budgetRepository.save(budget);
        return modelMapper.map(savedBudget, BudgetResponseDTO.class);
    }

    @Override
    @Transactional
    public List<BudgetResponseDTO> createMultipleBudgets(Long userId, List<BudgetRequestDTO> budgetDTOs) {
        // 1. Fetch User once
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        // Calculate dates once
        LocalDate[] period = calculateBudgetPeriod(user, LocalDate.now());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];

        LocalDateTime now = LocalDateTime.now();

        Set<Long> categoriesInBatch = new HashSet<>();
        List<Budget> budgetsToSave = new java.util.ArrayList<>();

        for (BudgetRequestDTO dto : budgetDTOs) {
            // Check for duplicates within the submitted request
            if (!categoriesInBatch.add(dto.getCategoryId())) {
                throw new APIException("Category ID " + dto.getCategoryId() + " is listed more than once in the batch request.");
            }

            // 2. Fetch Category
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new APIException("Category not found with id: " + dto.getCategoryId()));

            // **KEY CHANGE: Attempt to find existing budget (UPSERT LOGIC)**
            Optional<Budget> existingBudgetOpt = budgetRepository.findByUserIdAndCategoryAndDateOverlap(
                    userId,
                    dto.getCategoryId(),
                    startDate,
                    endDate
            );

            Budget budget;
            if (existingBudgetOpt.isPresent()) {
                // UPDATE existing budget
                budget = existingBudgetOpt.get();
                budget.setAmount(dto.getAmount());
                budget.setUpdatedAt(now);
            } else {
                // CREATE new budget
                budget = new Budget();
                budget.setAmount(dto.getAmount());
                budget.setStartDate(startDate);
                budget.setEndDate(endDate);
                budget.setCategory(category);
                budget.setUser(user);
                budget.setCreatedAt(now);
                budget.setUpdatedAt(now);
            }

            budgetsToSave.add(budget);
        }

        // saveAll handles both new (insert, if budget.id is null) and existing (update, if budget.id is set) entities
        List<Budget> savedBudgets = budgetRepository.saveAll(budgetsToSave);

        return savedBudgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetResponseDTO updateBudget(Long userId, Long budgetId, BudgetRequestDTO budgetDTO) {
        Budget existingBudget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found with id " + budgetId + " for this user."));

        // If the category ID is different, fetch and set the new category
        if (!existingBudget.getCategory().getId().equals(budgetDTO.getCategoryId())) {
            Category newCategory = categoryRepository.findById(budgetDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + budgetDTO.getCategoryId()));
            existingBudget.setCategory(newCategory);
        }

        // Update amount. Dates remain locked to the original period.
        existingBudget.setAmount(budgetDTO.getAmount());
        existingBudget.setUpdatedAt(LocalDateTime.now());

        Budget updatedBudget = budgetRepository.save(existingBudget);
        return modelMapper.map(updatedBudget, BudgetResponseDTO.class);
    }

    // getBudgetById and deleteBudget are the same as before.
    @Override
    @Transactional(readOnly = true)
    public BudgetResponseDTO getBudgetById(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found with id " + budgetId + " for this user."));
        return modelMapper.map(budget, BudgetResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget existingBudget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found with id " + budgetId + " for this user."));

        budgetRepository.delete(existingBudget);
    }
}