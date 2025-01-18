package com.example.onboarding.auth.controller;

import com.example.onboarding.auth.dto.request.SignRequest;
import com.example.onboarding.auth.dto.request.SignupRequest;
import com.example.onboarding.auth.dto.response.SignResponse;
import com.example.onboarding.auth.dto.response.SignupResponse;
import com.example.onboarding.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @PostMapping("/sign")
    public ResponseEntity<SignResponse> sign(@RequestBody SignRequest signRequest) {
        return ResponseEntity.ok(authService.sign(signRequest));
    }
}
