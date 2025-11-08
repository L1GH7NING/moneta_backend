package com.track.moneta.backend.controllers;

import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.payload.LoginRequest;
import com.track.moneta.backend.payload.SignUpRequest;
import com.track.moneta.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // We can inject this value to control the 'secure' flag of the cookie
    @Value("${app.cookie.secure:false}") // Default to false for dev environments
    private boolean cookieSecure;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        UserDTO createdUser = authService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        // The service already returns the token and can give us the user
        Map<String, Object> authResponse = authService.login(loginRequest);
        String jwt = (String) authResponse.get("token");
        UserDTO userDTO = (UserDTO) authResponse.get("user");

        ResponseCookie cookie = ResponseCookie.from("auth-token", jwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();

        // Return the cookie in the header AND the user data in the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userDTO);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuthCodeLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        Map<String, Object> authResponse = authService.googleLoginWithAuthCode(code);
        String jwt = (String) authResponse.get("token");
        UserDTO userDTO = (UserDTO) authResponse.get("user");

        // Create the cookie, just like in the regular login
        ResponseCookie cookie = ResponseCookie.from("auth-token", jwt)
                .httpOnly(true)
                .secure(cookieSecure) // Use true in production (HTTPS)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userDTO);
    }

    // --- NEW LOGOUT ENDPOINT ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Create a cookie that expires immediately, effectively deleting it from the browser
        ResponseCookie cookie = ResponseCookie.from("auth-token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // 👈 Set max age to 0 to expire the cookie
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logout successful!"));
    }
}