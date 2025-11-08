//package com.track.moneta.backend.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.track.moneta.backend.config.SecurityConfig;
//import com.track.moneta.backend.dto.UserDTO;
//import com.track.moneta.backend.payload.LoginRequest;
//import com.track.moneta.backend.payload.SignUpRequest;
//import com.track.moneta.backend.services.AuthService;
//import com.track.moneta.backend.utility.JwtUtil;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthController.class)
//@Import(SecurityConfig.class)
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private AuthService authService;
//
//    @MockitoBean
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void signup_shouldReturnCreatedUser() throws Exception {
//        SignUpRequest signUpRequest = new SignUpRequest();
//        signUpRequest.setName("Test User");
//        signUpRequest.setEmail("test@example.com");
//        signUpRequest.setPassword("password");
//
//        UserDTO userDTO = new UserDTO();
//        userDTO.setName("Test User");
//        userDTO.setEmail("test@example.com");
//
//        Mockito.when(authService.signup(Mockito.any(SignUpRequest.class))).thenReturn(userDTO);
//
//        mockMvc.perform(post("/api/auth/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signUpRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name").value("Test User"))
//                .andExpect(jsonPath("$.email").value("test@example.com"));
//    }
//
//    @Test
//    void login_shouldSetCookieAndReturnUserDTO() throws Exception {
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail("test@example.com");
//        loginRequest.setPassword("password");
//
//        String token = "test-jwt-token";
//        UserDTO userDTO = new UserDTO(1L, "Test User", "test@example.com");
//
//        Map<String, Object> authServiceResponse = new HashMap<>();
//        authServiceResponse.put("token", token);
//        authServiceResponse.put("user", userDTO);
//
//        Mockito.when(authService.login(Mockito.any(LoginRequest.class))).thenReturn(authServiceResponse);
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                // Assertions for the cookie remain the same and are correct
//                .andExpect(cookie().exists("auth-token"))
//                .andExpect(cookie().value("auth-token", token))
//                .andExpect(cookie().httpOnly("auth-token", true))
//                .andExpect(cookie().path("auth-token", "/"))
//                // Corrected assertions for the JSON body (UserDTO)
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.name").value("Test User"))
//                .andExpect(jsonPath("$.email").value("test@example.com"));
//    }
//
//    @Test
//    void signup_shouldReturnBadRequestForInvalidInput() throws Exception {
//        SignUpRequest signUpRequest = new SignUpRequest();
//        signUpRequest.setName(""); // Invalid name
//        signUpRequest.setEmail(""); // Invalid email
//        signUpRequest.setPassword(""); // Invalid password
//
//        mockMvc.perform(post("/api/auth/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signUpRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void login_shouldReturnBadRequestForInvalidInput() throws Exception {
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail(""); // Invalid email
//        loginRequest.setPassword(""); // Invalid password
//
//        mockMvc.perform(post("/api/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isBadRequest());
//    }
//
//}
