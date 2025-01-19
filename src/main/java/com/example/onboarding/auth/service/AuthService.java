package com.example.onboarding.auth.service;

import com.example.onboarding.auth.dto.request.SignRequest;
import com.example.onboarding.auth.dto.request.SignupRequest;
import com.example.onboarding.auth.dto.response.SignResponse;
import com.example.onboarding.auth.dto.response.SignupResponse;
import com.example.onboarding.common.config.JwtUtil;
import com.example.onboarding.common.exception.InvalidRequestException;
import com.example.onboarding.common.exception.TokenStorageException;
import com.example.onboarding.user.entity.User;
import com.example.onboarding.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private static final String ACCESS_TOKEN_PREFIX = "ACCESS_TOKEN_";
    private static final String REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN_";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 24L;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        log.info("회원가입 시도: username={}", signupRequest.getUsername());

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new InvalidRequestException("Already exists username");
        }

        validateNewPassword(signupRequest.getPassword());

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User newUser = User.from(signupRequest, encodedPassword);
        User savedUser = userRepository.save(newUser);

        log.info("회원가입 성공: username={}", savedUser.getUsername());

        return new SignupResponse(
                savedUser.getUsername(),
                savedUser.getNickname(),
                List.of(new SimpleGrantedAuthority(savedUser.getUserRole().name())));
    }

    @Transactional
    public SignResponse sign(SignRequest signRequest) {
        log.info("로그인 시도: username={}", signRequest.getUsername());

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

        log.info("로그인 성공: username={}", user.getUsername());

        return new SignResponse(accessToken);
    }

    public String createAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), user.getUserRole());
    }

    public String createRefreshToken(User user) {
        return jwtUtil.createRefreshToken(user.getId());
    }

    public void saveTokens(String userId, String accessToken, String refreshToken) {
        try {
            redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + userId, accessToken,
                    ACCESS_TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken,
                    REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("토큰 저장 실패: userId={}", userId, e);
            throw new TokenStorageException("토큰 저장에 실패했습니다.");
        }
    }

    private void validateNewPassword(String password) {
        if (password.length() < 8 ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[A-Z].*")) {
            throw new InvalidRequestException("비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다");
        }
    }
}
