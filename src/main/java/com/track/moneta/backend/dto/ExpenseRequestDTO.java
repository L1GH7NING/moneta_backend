package com.track.moneta.backend.dto;

import com.track.moneta.backend.enums.ExpenseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;

    // Default is VARIABLE in the entity, but good to allow client override
    private ExpenseType type = ExpenseType.VARIABLE;

    @NotNull(message = "Expense date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    private Long categoryId;

}