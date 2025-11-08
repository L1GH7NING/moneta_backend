package com.track.moneta.backend.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.exceptions.APIException;
import com.track.moneta.backend.models.User;
import com.track.moneta.backend.payload.LoginRequest;
import com.track.moneta.backend.payload.SignUpRequest;
import com.track.moneta.backend.repositories.UserRepository;
import com.track.moneta.backend.services.AuthService;
import com.track.moneta.backend.utility.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${google.client.secret}")
    private String GOOGLE_CLIENT_SECRET;

    private static final String GOOGLE_REDIRECT_URI = "postmessage";


    @Override
    public UserDTO signup(@Valid SignUpRequest signUpRequest) {
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if(existingUser.isPresent()) {
            throw new APIException("Email is already in use");
        }
        User user = new User();
        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new APIException("User does not exist"));
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new APIException("Invalid credentials");
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        String token = jwtUtil.generateToken(userDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userDTO);
        return response;
    }

    @Override
    public Map<String, Object> googleLoginWithAuthCode(String code) {
        try {
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            NetHttpTransport httpTransport = new NetHttpTransport();

            // Exchange the authorization code for tokens
            String idTokenString = getIdTokenString(code, httpTransport, jsonFactory);

            // Verify the ID token (same logic as before)
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    httpTransport,
                    jsonFactory)
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new APIException("Invalid Google ID token after code exchange.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setCreatedAt(LocalDateTime.now());
                newUser.setUpdatedAt(LocalDateTime.now());
                return userRepository.save(newUser);
            });

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String token = jwtUtil.generateToken(userDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", userDTO);
            return response;

        } catch (IOException e) {
            throw new APIException("Google code exchange failed: " + e.getMessage());
        } catch (Exception e) {
            throw new APIException("Google login with auth code failed: " + e.getMessage());
        }
    }

    private String getIdTokenString(String code, NetHttpTransport httpTransport, JsonFactory jsonFactory) throws IOException {
        GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                GOOGLE_CLIENT_ID,
                GOOGLE_CLIENT_SECRET,
                code,
                GOOGLE_REDIRECT_URI
        );

        GoogleTokenResponse tokenResponse = tokenRequest.execute();

        String idTokenString = tokenResponse.getIdToken();
        if (idTokenString == null) {
            throw new APIException("Failed to retrieve ID token from Google.");
        }
        return idTokenString;
    }


}
