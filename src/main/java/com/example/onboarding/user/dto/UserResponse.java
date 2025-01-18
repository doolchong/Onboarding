package com.example.onboarding.user.dto;

import com.example.onboarding.common.dto.AuthUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserResponse(AuthUser authUser) {
        id = authUser.getId();
        username = authUser.getUsername();
        nickname = authUser.getNickname();
        authorities = authUser.getAuthorities();
    }
}
