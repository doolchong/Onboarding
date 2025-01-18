package com.example.onboarding.user.entity;

import com.example.onboarding.auth.dto.request.SignupRequest;
import com.example.onboarding.common.dto.Timestamped;
import com.example.onboarding.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private User(SignupRequest signupRequest, String encodedPassword) {
        username = signupRequest.getUsername();
        password = encodedPassword;
        nickname = signupRequest.getNickname();
        userRole = UserRole.ROLE_USER;
    }

    public static User from(SignupRequest signupRequest, String encodedPassword) {
        return new User(signupRequest, encodedPassword);
    }
}
