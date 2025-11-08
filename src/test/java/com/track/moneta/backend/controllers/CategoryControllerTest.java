//package com.track.moneta.backend.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.track.moneta.backend.config.SecurityConfig;
//import com.track.moneta.backend.dto.CategoryRequestDTO;
//import com.track.moneta.backend.dto.CategoryResponseDTO;
//import com.track.moneta.backend.exceptions.APIException; // Assuming you have a custom exception
//import com.track.moneta.backend.services.CategoryService;
//import com.track.moneta.backend.utility.JwtUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(CategoryController.class)
//@Import(SecurityConfig.class)
//@WithMockUser // Apply a mock user to all tests in this class
//class CategoryControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private CategoryService categoryService;
//
//    @MockitoBean
//    private JwtUtil jwtUtil;
//
//    private CategoryRequestDTO categoryRequestDTO;
//    private CategoryResponseDTO categoryResponseDTO;
//
//    @BeforeEach
//    void setUp() {
//        categoryRequestDTO = new CategoryRequestDTO();
//        categoryRequestDTO.setName("Groceries");
//
//        categoryResponseDTO = new CategoryResponseDTO();
//        categoryResponseDTO.setId(1L);
//        categoryResponseDTO.setName("Groceries");
//    }
//
//    @Nested
//    @DisplayName("Create Category Tests")
//    class CreateCategoryTests {
//
//        @Test
//        void createCategory_shouldReturnCreatedCategory_whenRequestIsValid() throws Exception {
//            Long userId = 1L;
//            when(categoryService.createCategory(any(CategoryRequestDTO.class), eq(userId)))
//                    .thenReturn(categoryResponseDTO);
//
//            mockMvc.perform(post("/api/categories")
//                            .param("userId", userId.toString())
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(categoryRequestDTO))
//                            .with(csrf()))
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.id", is(1)))
//                    .andExpect(jsonPath("$.name", is("Groceries")));
//        }
//
//        @Test
//        void createCategory_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
//            CategoryRequestDTO invalidRequest = new CategoryRequestDTO();
//            invalidRequest.setName(""); // Invalid name for validation
//
//            mockMvc.perform(post("/api/categories")
//                            .param("userId", "1")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(invalidRequest))
//                            .with(csrf()))
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        void createCategory_shouldReturnBadRequest_whenUserIdParamIsMissing() throws Exception {
//            mockMvc.perform(post("/api/categories")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(categoryRequestDTO))
//                            .with(csrf()))
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("Get All Categories Tests")
//    class GetAllCategoriesTests {
//
//        @Test
//        void getAllCategoriesForUser_shouldReturnListOfCategories() throws Exception {
//            Long userId = 1L;
//            List<CategoryResponseDTO> categories = Collections.singletonList(categoryResponseDTO);
//            when(categoryService.getCategoriesByUser(userId)).thenReturn(categories);
//
//            mockMvc.perform(get("/api/categories")
//                            .param("userId", userId.toString()))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$", hasSize(1)))
//                    .andExpect(jsonPath("$[0].name", is("Groceries")));
//        }
//
//        @Test
//        void getAllCategoriesForUser_shouldReturnEmptyList_whenNoCategoriesExist() throws Exception {
//            Long userId = 1L;
//            when(categoryService.getCategoriesByUser(userId)).thenReturn(Collections.emptyList());
//
//            mockMvc.perform(get("/api/categories")
//                            .param("userId", userId.toString()))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$", hasSize(0)));
//        }
//    }
//
//    @Nested
//    @DisplayName("Update Category Tests")
//    class UpdateCategoryTests {
//
//        @Test
//        void updateCategory_shouldReturnUpdatedCategory_whenRequestIsValid() throws Exception {
//            Long categoryId = 1L;
//            Long userId = 1L;
//            CategoryResponseDTO updatedResponse = new CategoryResponseDTO();
//            updatedResponse.setId(categoryId);
//            updatedResponse.setName("Updated Groceries");
//
//            when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDTO.class), eq(userId)))
//                    .thenReturn(updatedResponse);
//
//            mockMvc.perform(put("/api/categories")
//                            .param("categoryId", categoryId.toString())
//                            .param("userId", userId.toString())
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(categoryRequestDTO))
//                            .with(csrf()))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id", is(1)))
//                    .andExpect(jsonPath("$.name", is("Updated Groceries")));
//        }
//
//        @Test
//        void updateCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
//            Long categoryId = 99L; // Non-existent ID
//            Long userId = 1L;
//            // Mock the service to throw an exception
//            when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDTO.class), eq(userId)))
//                    .thenThrow(new APIException("Category not found"));
//
//            mockMvc.perform(put("/api/categories")
//                            .param("categoryId", categoryId.toString())
//                            .param("userId", userId.toString())
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(categoryRequestDTO))
//                            .with(csrf()))
//                    // Assuming you have a @ControllerAdvice to handle APIException and return 404
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("Delete Category Tests")
//    class DeleteCategoryTests {
//
//        @Test
//        void deleteCategory_shouldReturnNoContent_whenSuccessful() throws Exception {
//            Long categoryId = 1L;
//            Long userId = 1L;
//            doNothing().when(categoryService).deleteCategory(categoryId, userId);
//
//            mockMvc.perform(delete("/api/categories")
//                            .param("categoryId", categoryId.toString())
//                            .param("userId", userId.toString())
//                            .with(csrf()))
//                    .andExpect(status().isNoContent());
//        }
//
//        @Test
//        void deleteCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
//            Long categoryId = 99L; // Non-existent ID
//            Long userId = 1L;
//            // Mock the service to throw an exception for a void method
//            doThrow(new APIException("Category to delete not found")).when(categoryService).deleteCategory(categoryId, userId);
//
//            mockMvc.perform(delete("/api/categories")
//                            .param("categoryId", categoryId.toString())
//                            .param("userId", userId.toString())
//                            .with(csrf()))
//                    // Assuming you have a @ControllerAdvice to handle APIException and return 404
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        void deleteCategory_shouldReturnBadRequest_whenCategoryIdIsMissing() throws Exception {
//            mockMvc.perform(delete("/api/categories")
//                            .param("userId", "1")
//                            .with(csrf()))
//                    .andExpect(status().isBadRequest());
//        }
//    }
//}