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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

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

    @Transactional
    public SignResponse sign(SignRequest signRequest) {
        User user = userRepository.findByUsername(signRequest.getUsername()).orElseThrow(
                () -> new InvalidRequestException("Invalid username"));

        if (!passwordEncoder.matches(signRequest.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid password");
        }

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        saveTokens(user.getId().toString(), accessToken, refreshToken);

        jwtUtil.setAccessTokenCookie(accessToken);
        jwtUtil.setRefreshTokenCookie(refreshToken);

        return new SignResponse(accessToken);
    }

    public String createAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), user.getUserRole());
    }

    public String createRefreshToken(User user) {
        return jwtUtil.createRefreshToken(user.getId());
    }

    public void saveTokens(String userId, String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set("ACCESS_TOKEN_" + userId, accessToken, 60, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("REFRESH_TOKEN_" + userId, refreshToken, 1, TimeUnit.DAYS);
    }
}
