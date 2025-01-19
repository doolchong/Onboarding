package com.example.onboarding.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class SignupResponse {

    @NotBlank
    @Schema(description = "사용자명", example = "user123")
    private final String username;

    @NotBlank
    @Schema(description = "닉네임", example = "홍길동")
    private final String nickname;

    @NotNull
    @Schema(description = "사용자 권한 목록", example = "[\"ROLE_USER\"]")
    private final Collection<? extends GrantedAuthority> authorities;
}
