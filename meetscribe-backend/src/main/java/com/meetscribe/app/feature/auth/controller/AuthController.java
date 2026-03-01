package com.meetscribe.app.feature.auth.controller;

import com.meetscribe.app.common.response.ApiResponse;
import com.meetscribe.app.feature.auth.dto.LoginRequest;
import com.meetscribe.app.feature.auth.dto.LoginResponse;
import com.meetscribe.app.feature.auth.dto.RefreshRequest;
import com.meetscribe.app.feature.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(
                request.email(),
                request.password(),
                request.deviceId()
        );

        return ApiResponse.success(
               response
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @RequestBody RefreshRequest request
    ) {

        LoginResponse response =
                authService.refresh(request.refreshToken());

        return ApiResponse.success(response);
    }
}