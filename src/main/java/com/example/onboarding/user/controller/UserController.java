package com.example.onboarding.user.controller;

import com.example.onboarding.common.dto.AuthUser;
import com.example.onboarding.user.dto.UserResponse;
import com.example.onboarding.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<UserResponse> signup(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUser(authUser));
    }
}
