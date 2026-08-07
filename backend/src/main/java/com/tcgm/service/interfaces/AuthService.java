package com.tcgm.service;

import com.tcgm.dto.request.LoginRequest;
import com.tcgm.dto.request.RegisterRequest;
import com.tcgm.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    void logout(String token);
    AuthResponse refreshToken(String refreshToken);
}