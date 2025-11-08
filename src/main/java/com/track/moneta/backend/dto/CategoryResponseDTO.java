package com.track.moneta.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CategoryResponseDTO> subcategories; // To show nested structure
}