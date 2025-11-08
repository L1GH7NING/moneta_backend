package com.track.moneta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 20, message = "Category name must be less than 100 characters")
    private String name;

    private Long parentId; // Null for main categories, ID for subcategories
}