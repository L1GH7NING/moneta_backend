package com.track.moneta.backend.dto;

import com.track.moneta.backend.enums.ExpenseType;
import lombok.AllArgsConstructor; // <--- ADD THIS (ensures the all-args is still there for @Builder/JPA)
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; // <--- ADD THIS (fixes the ModelMapper error)

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {

    private Long id;
    private BigDecimal amount;
    private String description;
    private ExpenseType type;
    private LocalDate expenseDate;

    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}