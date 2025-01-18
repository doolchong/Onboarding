package com.example.onboarding.common.config;

import com.example.onboarding.common.exception.ServerException;
import com.example.onboarding.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "refreshToken";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 1시간
    private static final long REFRESH_TOKEN_TIME = 24 * 60 * 60 * 1000L; // 1일
    private final HttpServletResponse httpServletResponse;

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(Long userId, String username, String nickname, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("username", username)
                        .claim("nickname", nickname)
                        .claim("userRole", userRole)
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        return BEARER_PREFIX + Jwts.builder()
                .setSubject(Long.toString(userId))  // 사용자 ID 설정
                .setIssuedAt(now)  // 발급 시간
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_TIME))  // 만료 시간 설정
                .signWith(key, signatureAlgorithm)  // 서명 알고리즘과 키로 서명
                .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new ServerException("Not Found Token");
    }

    // 쿠키에 토큰 넣기
    public void addJwtToCookie(String token) {
        try {
            String encodeToken = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie cookie = new Cookie("Authorization", encodeToken); // Name-Value
            cookie.setHttpOnly(true); // 자바스크립트에서 쿠키에 접근할 수 없도록 설정
            cookie.setMaxAge(60 * 60); // 쿠키의 유효 기간 설정(1시간)
            cookie.setPath("/");

            // Response 객체에 Cookie 추가
            httpServletResponse.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            log.error("JWT 쿠키 생성 중 에러 발생", e.getMessage());
        }
    }

    // HttpServletRequest 에서 Cookie Value : JWT 가져오기
    public String getTokenFromRequest(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Access Token을 쿠키에 저장 (Bearer prefix 없이)
    public void setAccessTokenCookie(String token) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token);
            cookie.setHttpOnly(true);  // XSS 공격 방지를 위해 HttpOnly 설정. HttpOnly로 설정하여 JavaScript에서 접근 불가
            cookie.setMaxAge(24 * 60 * 60);  // Access Token 쿠키의 만료 시간 1일로 설정 (Access Token 만료 시간과는 별개)
            cookie.setPath("/");  // 쿠키의 경로를 루트로 설정
            cookie.setSecure(true);
            cookie.setDomain("localhost");
            // Set-Cookie 헤더에 SameSite 속성 추가
            httpServletResponse.addHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue() +
                    "; HttpOnly; Max-Age=" + cookie.getMaxAge() + "; Path=" + cookie.getPath() +
                    "; Secure; SameSite=Strict");
            httpServletResponse.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding access token cookie value", e);
        }
    }

    // Refresh Token 쿠키 설정
    public void setRefreshTokenCookie(String refreshToken) {
        try {
            refreshToken = URLEncoder.encode(refreshToken, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행
            Cookie cookie = new Cookie(REFRESH_TOKEN_HEADER, refreshToken);
            cookie.setHttpOnly(true);        // XSS 공격 방지를 위해 HttpOnly 설정. HttpOnly로 설정하여 JavaScript에서 접근 불가
            cookie.setMaxAge(1 * 24 * 60 * 60);  // Refresh Token 쿠키의 만료 시간 1일로 설정 (Refresh Token 만료 시간과는 별개)
            cookie.setPath("/");             // 쿠키의 경로를 루트로 설정
            cookie.setSecure(true);
            cookie.setDomain("localhost");
            // Set-Cookie 헤더에 SameSite 속성 추가
            httpServletResponse.addHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue() +
                    "; HttpOnly; Max-Age=" + cookie.getMaxAge() + "; Path=" + cookie.getPath() +
                    "; Secure; SameSite=Strict");
            httpServletResponse.addCookie(cookie);
            httpServletResponse.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding refresh token cookie value", e);
        }
    }
}