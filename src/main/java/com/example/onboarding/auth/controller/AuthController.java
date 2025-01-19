package com.example.onboarding.auth.controller;

import com.example.onboarding.auth.dto.request.SignRequest;
import com.example.onboarding.auth.dto.request.SignupRequest;
import com.example.onboarding.auth.dto.response.SignResponse;
import com.example.onboarding.auth.dto.response.SignupResponse;
import com.example.onboarding.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입 API") // api 문서화
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(signupRequest));
    }

    @Operation(summary = "로그인 API") // api 문서화
    @PostMapping("/sign")
    public ResponseEntity<SignResponse> sign(@Valid @RequestBody SignRequest signRequest) {
        return ResponseEntity.ok(authService.sign(signRequest));
    }
}
