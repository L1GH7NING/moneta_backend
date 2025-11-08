package com.track.moneta.backend.services.impl;

import com.track.moneta.backend.dto.CategoryRequestDTO;
import com.track.moneta.backend.dto.CategoryResponseDTO;
import com.track.moneta.backend.exceptions.APIException;
import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.models.User;
import com.track.moneta.backend.repositories.CategoryRepository;
import com.track.moneta.backend.repositories.UserRepository;
import com.track.moneta.backend.services.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found"));
        Category parent = null;
        if (categoryDto.getParentId() != null) {
            parent = categoryRepository.findByIdAndUserId(categoryDto.getParentId(), userId)
                    .orElseThrow(() -> new APIException("Parent category not found"));
        }

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setUser(user);
        category.setParent(parent);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return modelMapper.map(categoryRepository.save(category), CategoryResponseDTO.class);
    }

    @Override
    public List<CategoryResponseDTO> getCategoriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found"));
        List<Category> categories = categoryRepository.findTopLevelCategoriesWithSubcategories(userId);
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponseDTO.class))
                .toList();
    }

    @Override
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO requestDto, Long userId) {
        Category existingCategory = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new APIException("Category id not found for category id: " + categoryId + " and user id: " + userId));

        existingCategory.setName(requestDto.getName());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return modelMapper.map(updatedCategory, CategoryResponseDTO.class);
    }

    @Override
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new APIException("Category not found or does not belong to user"));
        categoryRepository.delete(category);
    }
}
