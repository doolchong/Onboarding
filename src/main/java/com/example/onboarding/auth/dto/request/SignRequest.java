package com.example.onboarding.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class SignRequest {

    @NotNull
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 4, max = 20, message = "사용자명은 4-20자 사이여야 합니다")
    @Schema(description = "사용자명", example = "user123") // api 문서화
    private final String username;

    @NotNull
    @ToString.Exclude // 로그 출력 시 비밀번호가 노출되지 않도록
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // JSON 직렬화 시 비밀번호 제외
    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "비밀번호", example = "Password123!") // api 문서화
    private final String password;
}
