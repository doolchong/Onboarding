package com.example.onboarding.common.dto;

import com.example.onboarding.user.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AuthUser {

    private final Long id;
    private final String username;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public static AuthUser from(Long id, String username, String nickname, UserRole userRole) {
        return new AuthUser(id, username, nickname, List.of(new SimpleGrantedAuthority(userRole.name())));
    }
}