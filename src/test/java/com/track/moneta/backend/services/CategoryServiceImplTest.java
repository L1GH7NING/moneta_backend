//package com.track.moneta.backend.services;
//
//import com.track.moneta.backend.dto.CategoryRequestDTO;
//import com.track.moneta.backend.dto.CategoryResponseDTO;
//import com.track.moneta.backend.exceptions.APIException;
//import com.track.moneta.backend.models.Category;
//import com.track.moneta.backend.models.User;
//import com.track.moneta.backend.repositories.CategoryRepository;
//import com.track.moneta.backend.repositories.UserRepository;
//import com.track.moneta.backend.services.impl.CategoryServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryServiceImplTest {
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private CategoryServiceImpl categoryService;
//
//    private User user;
//    private Category parentCategory;
//    private Category newCategory;
//    private CategoryRequestDTO categoryRequestDTO;
//    private CategoryResponseDTO categoryResponseDTO;
//
//    @BeforeEach
//    void setUp() {
//        // Common setup for tests
//        user = new User();
//        user.setId(1L);
//        user.setName("testuser");
//
//        parentCategory = new Category();
//        parentCategory.setId(10L);
//        parentCategory.setName("Parent");
//        parentCategory.setUser(user);
//
//        categoryRequestDTO = new CategoryRequestDTO();
//        categoryRequestDTO.setName("New Category");
//
//        newCategory = new Category();
//        newCategory.setId(1L);
//        newCategory.setName("New Category");
//        newCategory.setUser(user);
//        newCategory.setCreatedAt(LocalDateTime.now());
//        newCategory.setUpdatedAt(LocalDateTime.now());
//
//        categoryResponseDTO = new CategoryResponseDTO();
//        categoryResponseDTO.setId(1L);
//        categoryResponseDTO.setName("New Category");
//    }
//
//    @Test
//    void createCategory_shouldCreateTopLevelCategory_whenParentIdIsNull() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
//        when(modelMapper.map(newCategory, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);
//
//        // Act
//        CategoryResponseDTO result = categoryService.createCategory(categoryRequestDTO, 1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("New Category", result.getName());
//        verify(userRepository, times(1)).findById(1L);
//        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
//        verify(categoryRepository, times(1)).save(any(Category.class));
//        verify(modelMapper, times(1)).map(any(Category.class), eq(CategoryResponseDTO.class));
//    }
//
//    @Test
//    void createCategory_shouldCreateSubCategory_whenParentIdIsProvided() {
//        // Arrange
//        categoryRequestDTO.setParentId(10L);
//        newCategory.setParent(parentCategory);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(categoryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(parentCategory));
//        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
//        when(modelMapper.map(newCategory, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);
//
//        // Act
//        CategoryResponseDTO result = categoryService.createCategory(categoryRequestDTO, 1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("New Category", result.getName());
//        verify(userRepository, times(1)).findById(1L);
//        verify(categoryRepository, times(1)).findByIdAndUserId(10L, 1L);
//        verify(categoryRepository, times(1)).save(any(Category.class));
//    }
//
//    @Test
//    void createCategory_shouldThrowAPIException_whenUserNotFound() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        APIException exception = assertThrows(APIException.class, () -> {
//            categoryService.createCategory(categoryRequestDTO, 1L);
//        });
//
//        assertEquals("User not found", exception.getMessage());
//        verify(categoryRepository, never()).save(any());
//    }
//
//    @Test
//    void createCategory_shouldThrowAPIException_whenParentCategoryNotFound() {
//        // Arrange
//        categoryRequestDTO.setParentId(99L); // Non-existent parent ID
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        APIException exception = assertThrows(APIException.class, () -> {
//            categoryService.createCategory(categoryRequestDTO, 1L);
//        });
//
//        assertEquals("Parent category not found", exception.getMessage());
//        verify(categoryRepository, never()).save(any());
//    }
//
//    @Test
//    void getCategoriesByUser_shouldReturnEmptyList() {
//        // Arrange
//        Long userId = 1L;
//
//        // 1. Tell the mock to find the user
//        // The 'user' object is created in @BeforeEach with ID 1L
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        // 2. Tell the mock to find NO categories for this user
//        // The categories result is already a List<Category>, and we want an empty one
//        when(categoryRepository.findTopLevelCategoriesWithSubcategories(userId))
//                .thenReturn(Collections.emptyList());
//
//        // 3. Since no categories are found, ModelMapper is not called, so we don't need to stub it.
//
//        // Act
//        List<CategoryResponseDTO> result = categoryService.getCategoriesByUser(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//
//        // Optional: Verify the methods were called
//        verify(userRepository, times(1)).findById(userId);
//        verify(categoryRepository, times(1)).findTopLevelCategoriesWithSubcategories(userId);
//    }
//
//    // NOTE: The following test is an example for when the implementation of getCategoriesByUser is completed.
//    /*
//    @Test
//    void getCategoriesByUser_shouldReturnListOfCategories_whenUserHasCategories() {
//        // Arrange
//        Long userId = 1L;
//        List<Category> categories = List.of(newCategory);
//        when(categoryRepository.findByUserId(userId)).thenReturn(categories); // Assuming a method like findByUserId exists
//        when(modelMapper.map(newCategory, CategoryResponseDTO.class)).thenReturn(categoryResponseDTO);
//
//        // Act
//        List<CategoryResponseDTO> result = categoryService.getCategoriesByUser(userId);
//
//        // Assert
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.size());
//        assertEquals("New Category", result.get(0).getName());
//        verify(categoryRepository, times(1)).findByUserId(userId);
//    }
//    */
//}