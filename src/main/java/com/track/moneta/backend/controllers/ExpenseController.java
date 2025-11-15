package com.track.moneta.backend.controllers;

import com.track.moneta.backend.dto.*;
import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.payload.CategoryExpense;
import com.track.moneta.backend.services.ExpenseService;
import com.track.moneta.backend.utility.ExcelGeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    private final ExcelGeneratorService excelGeneratorService;

    private Long getUserId(UserDTO currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Authentication principal (UserDTO) not available or missing ID.");
        }
        return currentUser.getId();
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO request,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = getUserId(currentUser);
        ExpenseResponseDTO newExpense = expenseService.createExpense(userId, request);
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses(
            @AuthenticationPrincipal UserDTO currentUser,
            @ModelAttribute ExpenseFilterDTO filter
    ) {
        Long userId = getUserId(currentUser);
        Page<ExpenseResponseDTO> expensePage = expenseService.getAllExpenses(userId, filter);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(expensePage.getTotalElements()));
        headers.add("Access-Control-Expose-Headers", "X-Total-Count"); // Important for CORS

        return ResponseEntity.ok()
                .headers(headers)
                .body(expensePage.getContent());
    }

    // GET /api/expenses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = getUserId(currentUser);
        ExpenseResponseDTO expense = expenseService.getExpenseById(id, userId);
        return ResponseEntity.ok(expense);
    }

    // PUT /api/expenses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDTO request,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = getUserId(currentUser);
        ExpenseResponseDTO updatedExpense = expenseService.updateExpense(id, userId, request);
        return ResponseEntity.ok(updatedExpense);
    }

    // DELETE /api/expenses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long userId = getUserId(currentUser);
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByDateFilter(
            @AuthenticationPrincipal UserDTO currentUser,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day
    ) {
        Long userId = getUserId(currentUser);
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByDateFilter(userId, year, month, day);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalExpensesInARange(
            @AuthenticationPrincipal UserDTO currentUser,
            @RequestParam(required = true) LocalDate startDate,
            @RequestParam(required = true) LocalDate endDate
    ){
        Long userId = getUserId(currentUser);
        Double totalExpense = expenseService.getTotalExpensesInRange(userId, startDate, endDate);
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/total/category")
    public ResponseEntity<List<CategoryExpense>> getTotalExpensesInARangeCategoryWise(
            @AuthenticationPrincipal UserDTO currentUser,
            @RequestParam(required = true) LocalDate startDate,
            @RequestParam(required = true) LocalDate endDate
    ){
        Long userId = getUserId(currentUser);
        List<CategoryExpense> totalExpense = expenseService.getTotalExpensesInRangeCategoryWise(userId, startDate, endDate);
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/total/daily")
    public ResponseEntity<List<DailyExpenseDTO>> getTotalExpensesGroupedDaily(
            @AuthenticationPrincipal UserDTO currentUser,
            @RequestParam(required = true) LocalDate startDate,
            @RequestParam(required = true) LocalDate endDate
    ) {
        Long userId = getUserId(currentUser);
        List<DailyExpenseDTO> dailyExpenses = expenseService.getTotalExpensesGroupedByDay(userId, startDate, endDate);
        return ResponseEntity.ok(dailyExpenses);
    }

    @GetMapping("/download/excel") // Changed endpoint to distinguish from CSV
    public void downloadMonthlyExpensesAsExcel(
            @AuthenticationPrincipal UserDTO currentUser,
            @RequestParam int year,
            @RequestParam int month,
            HttpServletResponse response) throws IOException {

        Long userId = getUserId(currentUser);

        // 1. Fetch the data using your existing service method (no changes here)
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByDateFilter(userId, year, month, null);

        // 2. Set HTTP headers for an Excel file download
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerValue = "attachment; filename=\"expenses-" + year + "-" + month + ".xlsx\"";
        response.setHeader("Content-Disposition", headerValue);

        // 3. Call the generator service to write the Excel file to the response stream
        excelGeneratorService.generateMonthlyExpenseExcel(expenses, year, month, response.getOutputStream());
    }

}