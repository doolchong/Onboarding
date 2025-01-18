package com.example.onboarding.auth.service;

import com.example.onboarding.auth.dto.request.SignRequest;
import com.example.onboarding.auth.dto.request.SignupRequest;
import com.example.onboarding.auth.dto.response.SignResponse;
import com.example.onboarding.auth.dto.response.SignupResponse;
import com.example.onboarding.common.config.JwtUtil;
import com.example.onboarding.common.exception.InvalidRequestException;
import com.example.onboarding.user.entity.User;
import com.example.onboarding.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new InvalidRequestException("Already exists username");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User newUser = User.from(signupRequest, encodedPassword);
        User savedUser = userRepository.save(newUser);

        return new SignupResponse(
                savedUser.getUsername(),
                savedUser.getNickname(),
                List.of(new SimpleGrantedAuthority(savedUser.getUserRole().name())));
    }

    public SignResponse sign(SignRequest signRequest) {
        User user = userRepository.findByUsername(signRequest.getUsername()).orElseThrow(
                () -> new InvalidRequestException("Invalid username"));

        if (!passwordEncoder.matches(signRequest.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid password");
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getUsername(), user.getNickname(), user.getUserRole());

        jwtUtil.addJwtToCookie(bearerToken);

        return new SignResponse(bearerToken);
    }
}
