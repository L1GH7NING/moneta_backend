package com.track.moneta.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetRequestDTO {

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "Budget amount cannot be null")
    @Positive(message = "Budget amount must be positive")
    private BigDecimal amount;
}