package com.example.onboarding.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class SignupResponse {

    private final String username;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;
}
