package com.track.moneta.backend.controllers;

import com.track.moneta.backend.dto.CategoryRequestDTO;
import com.track.moneta.backend.dto.CategoryResponseDTO;
import com.track.moneta.backend.dto.UserDTO; // Import your UserDTO
import com.track.moneta.backend.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // NEW IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO categoryDto,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        CategoryResponseDTO createdCategory = categoryService.createCategory(categoryDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategoriesForUser(
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesByUser(currentUser.getId());
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long categoryId, // Use PathVariable for the resource ID
            @Valid @RequestBody CategoryRequestDTO requestDto,
            @AuthenticationPrincipal UserDTO currentUser // Get user context from Security
    ) {
        // Pass the authenticated user's ID to the service, where ownership check occurs
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(
                categoryId,
                requestDto,
                currentUser.getId()
        );
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}") // Use PathVariable for the resource ID
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId, // Use PathVariable for the resource ID
            @AuthenticationPrincipal UserDTO currentUser // Get user context from Security
    ) {
        categoryService.deleteCategory(categoryId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}