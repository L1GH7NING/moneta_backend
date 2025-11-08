package com.track.moneta.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BudgetResponseDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}