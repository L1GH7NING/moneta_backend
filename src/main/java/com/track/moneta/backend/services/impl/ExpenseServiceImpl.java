package com.track.moneta.backend.services.impl;

import com.track.moneta.backend.dto.DailyExpenseDTO;
import com.track.moneta.backend.dto.ExpenseFilterDTO;
import com.track.moneta.backend.dto.ExpenseRequestDTO;
import com.track.moneta.backend.dto.ExpenseResponseDTO;
import com.track.moneta.backend.enums.ExpenseType; // Need this for manual mapping
import com.track.moneta.backend.exceptions.APIException; // Assumed
import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.models.Expense;
import com.track.moneta.backend.models.User;
import com.track.moneta.backend.payload.CategoryExpense;
import com.track.moneta.backend.repositories.CategoryRepository;
import com.track.moneta.backend.repositories.ExpenseRepository;
import com.track.moneta.backend.repositories.ExpenseSpecification;
import com.track.moneta.backend.repositories.UserRepository;
import com.track.moneta.backend.services.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(Long userId, ExpenseRequestDTO request) {
        // 1. Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        // 2. Fetch Category (if provided)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new APIException("Category not found with id: " + request.getCategoryId()));
        }

        // 3. Manually map fields from DTO to a new Expense Entity
        Expense expense = new Expense();

        // Map all fields manually
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setType(request.getType() != null ? request.getType() : ExpenseType.VARIABLE);
        expense.setExpenseDate(request.getExpenseDate());

        // 4. Set dependencies and audit fields
        expense.setUser(user);
        expense.setCategory(category); // Can be null
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        // 5. Save and Map back to Response DTO
        Expense savedExpense = expenseRepository.save(expense);

        return modelMapper.map(savedExpense, ExpenseResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDTO> getAllExpenses(Long userId, ExpenseFilterDTO filter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Expense> spec = ExpenseSpecification.hasUser(user);

        if (filter.getCategoryId() != null) {
            spec = spec.and(ExpenseSpecification.hasCategoryId(filter.getCategoryId()));
        }
        if (filter.getStartDate() != null) {
            spec = spec.and(ExpenseSpecification.hasStartDate(filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            spec = spec.and(ExpenseSpecification.hasEndDate(filter.getEndDate()));
        }

        Page<Expense> expensesPage = expenseRepository.findAll(spec, pageable);

        return expensesPage.map(expense -> modelMapper.map(expense, ExpenseResponseDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id, Long userId) {
        // Fetch User (for consistency)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id " + id + " for this user."));

        return modelMapper.map(expense, ExpenseResponseDTO.class);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, Long userId, ExpenseRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        Expense existingExpense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id " + id + " for this user."));

        existingExpense.setAmount(request.getAmount());
        existingExpense.setDescription(request.getDescription());
        existingExpense.setType(request.getType());
        existingExpense.setExpenseDate(request.getExpenseDate());


        if (request.getCategoryId() != null) {
            if (existingExpense.getCategory() == null || !existingExpense.getCategory().getId().equals(request.getCategoryId())) {
                Category newCategory = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new APIException("Category not found with id: " + request.getCategoryId()));
                existingExpense.setCategory(newCategory);
            }
        } else {
            existingExpense.setCategory(null);
        }

        Expense updatedExpense = expenseRepository.save(existingExpense);
        return modelMapper.map(updatedExpense, ExpenseResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, Long userId) {
        // Fetch User (for consistency)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        // Ensure the expense exists and belongs to the user before deleting
        Expense existingExpense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with id " + id + " for this user."));

        expenseRepository.delete(existingExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByDateFilter(Long userId, Integer year, Integer month, Integer day) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));
        LocalDate startDate;
        LocalDate endDate;

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        if (year == null && month == null && day == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        } else if (day != null && month != null) {
            try {
                startDate = LocalDate.of(targetYear, month, day);
                endDate = startDate;
            } catch (Exception e) {
                throw new APIException("Invalid date combination provided (Year: " + targetYear + ", Month: " + month + ", Day: " + day + ")");
            }
        } else if (month != null) {
            try {
                startDate = LocalDate.of(targetYear, month, 1);
                // Get the last day of the month
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } catch (Exception e) {
                throw new APIException("Invalid month/year combination provided (Year: " + targetYear + ", Month: " + month + ")");
            }
        } else if (year != null) {
            startDate = LocalDate.of(targetYear, 1, 1);
            endDate = LocalDate.of(targetYear, 12, 31);
        } else {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalExpensesInRange(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));
        if(startDate.isAfter(endDate)) throw new APIException("Start date cannot be greater than end date!");
        return expenseRepository.getTotalExpenseByUserAndDateRange(user, startDate, endDate).orElse(0.0);
    }

    @Override
    public List<CategoryExpense> getTotalExpensesInRangeCategoryWise(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));
        if(startDate.isAfter(endDate)) throw new APIException("Start date cannot be greater than end date!");
        List<Object[]> results = expenseRepository.getTotalExpensesGroupedByCategory(user, startDate, endDate);
        return results.stream()
                .map(row -> {
                    String category = (String) row[0];
                    Number sum = (Number) row[1];
                    Double totalAmount = sum.doubleValue();
                    return new CategoryExpense(category, totalAmount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyExpenseDTO> getTotalExpensesGroupedByDay(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));
        if (startDate.isAfter(endDate)) {
            throw new APIException("Start date cannot be after end date!");
        }

        List<Object[]> results = expenseRepository.getTotalExpensesGroupedByDay(user, startDate, endDate);

        return results.stream()
                .map(row -> {
                    LocalDate date = (LocalDate) row[0];
                    Double total = ((Number) row[1]).doubleValue(); // Cast from Number for flexibility
                    return new DailyExpenseDTO(date, total);
                })
                .collect(Collectors.toList());
    }

//    @Override
//    public List<ExpenseResponseDTO> getExpensesByCategory(Long userId, Long categoryId, int page, int size, String sortBy, String sortDir) {
//        return List.of();
//    }
}