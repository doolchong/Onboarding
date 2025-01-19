package com.example.onboarding.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class SignupRequest {

    @NotNull
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 4, max = 20, message = "사용자명은 4-20자 사이여야 합니다")
    @Schema(description = "사용자명", example = "user123") // api 문서화
    private final String username;

    @NotNull
    @ToString.Exclude // 로그 출력 시 비밀번호가 노출되지 않도록
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
    @Schema(description = "비밀번호", example = "Password123!") // api 문서화
    private final String password;

    @NotNull
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 10, message = "닉네임은 2-10자 사이여야 합니다")
    @Schema(description = "닉네임", example = "홍길동") // api 문서화
    private final String nickname;
}
