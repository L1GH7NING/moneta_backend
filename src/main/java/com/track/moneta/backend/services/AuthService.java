package com.track.moneta.backend.services;

import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.payload.LoginRequest;
import com.track.moneta.backend.payload.SignUpRequest;
import jakarta.validation.Valid;

import java.util.Map;

public interface AuthService {
    UserDTO signup(@Valid SignUpRequest signUpRequest);

    Map<String, Object> login(@Valid LoginRequest loginRequest);

    Map<String, Object> googleLoginWithAuthCode(String code);
}
