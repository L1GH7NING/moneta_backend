package com.track.moneta.backend.utility;

import com.track.moneta.backend.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set secret and expiration manually for testing
        jwtUtil.setSecretKey("my_super_secret_key_which_is_long_enough_12345");
        jwtUtil.setExpiration(1000 * 60 * 60); // 1 hour
    }

    @Test
    void testGetUserFromToken() {
        // Create sample user
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setName("Aditya M");
        user.setEmail("aditya@example.com");

        // Generate token
        String token = jwtUtil.generateToken(user);

        // Extract user from token
        UserDTO extractedUser = jwtUtil.getUserFromToken(token);

        // Assertions
        assertNotNull(extractedUser);
        assertEquals(user.getId(), extractedUser.getId());
        assertEquals(user.getName(), extractedUser.getName());
        assertEquals(user.getEmail(), extractedUser.getEmail());

        System.out.println("Token: " + token);
        System.out.println("Extracted User: " + extractedUser);
    }
}
