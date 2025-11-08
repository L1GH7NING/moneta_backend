package com.track.moneta.backend.services;

import com.track.moneta.backend.dto.CategoryRequestDTO;
import com.track.moneta.backend.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, Long userId);

    List<CategoryResponseDTO> getCategoriesByUser(Long userId);

    CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO requestDto, Long userId);

    void deleteCategory(Long categoryId, Long userId);
}
