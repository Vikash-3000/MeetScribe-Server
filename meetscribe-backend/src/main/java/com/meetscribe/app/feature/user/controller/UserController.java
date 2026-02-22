package com.meetscribe.app.feature.user.controller;

import com.meetscribe.app.common.response.ApiResponse;
import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.feature.user.dto.CreateUserRequest;
import com.meetscribe.app.feature.user.dto.UserResponse;
import com.meetscribe.app.feature.user.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserApplicationService service;

    public UserController(UserApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse create(
            @Valid @RequestBody CreateUserRequest request
    ) {

        User user = service.create(
                request.email(),
                request.password()
        );

        return ApiResponse.success(
                UserResponse.from(user)
        );

    }
}
