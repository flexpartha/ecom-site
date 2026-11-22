package com.ecom.api.service;

import com.ecom.api.dto.AuthResponse;
import com.ecom.api.dto.LoginRequest;
import com.ecom.api.dto.SignupRequest;
import com.ecom.api.dto.SignupResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    SignupResponse signup(SignupRequest request);
}
