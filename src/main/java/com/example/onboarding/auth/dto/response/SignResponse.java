package com.example.onboarding.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignResponse {

    @NotBlank(message = "토큰은 필수입니다")
    @Schema(description = "인증 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") // api 문서화
    private final String token;
}
