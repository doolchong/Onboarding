package com.example.onboarding.common.config;

import com.example.onboarding.common.exception.ServerException;
import com.example.onboarding.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtil jwtUtil;

    private final String TEST_SECRET_KEY = "c3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLWppd29vbi1zcHJpbmctYm9vdC1zZWN1cml0eS1qd3QtdHV0b3JpYWwK";

    @BeforeEach
    void setUp() throws Exception {
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        ReflectionUtils.makeAccessible(secretKeyField);
        secretKeyField.set(jwtUtil, TEST_SECRET_KEY);
        jwtUtil.init();
    }

    @Nested
    @DisplayName("Token 생성 테스트")
    class CreateTokenTest {

        @Test
        @DisplayName("Access Token 생성 성공")
        void createAccessTokenSuccess() {
            // given
            Long userId = 1L;
            String username = "test@test.com";
            String nickname = "tester";
            UserRole role = UserRole.ROLE_USER;

            // when
            String token = jwtUtil.createAccessToken(userId, username, nickname, role);

            // then
            assertThat(token).startsWith("Bearer ");
            String actualToken = jwtUtil.substringToken(token);
            Claims claims = jwtUtil.extractClaims(actualToken);

            assertThat(claims.getSubject()).isEqualTo(userId.toString());
            assertThat(claims.get("username")).isEqualTo(username);
            assertThat(claims.get("nickname")).isEqualTo(nickname);
            assertThat(claims.get("userRole")).isEqualTo(role.name());
        }

        @Test
        @DisplayName("Refresh Token 생성 성공")
        void createRefreshTokenSuccess() {
            // given
            Long userId = 1L;

            // when
            String token = jwtUtil.createRefreshToken(userId);

            // then
            assertThat(token).startsWith("Bearer ");
            String actualToken = jwtUtil.substringToken(token);
            Claims claims = jwtUtil.extractClaims(actualToken);
            assertThat(claims.getSubject()).isEqualTo(userId.toString());
        }
    }

    @Nested
    @DisplayName("Token 추출 테스트")
    class ExtractTokenTest {

        @Test
        @DisplayName("Bearer 토큰 추출 성공")
        void substringTokenSuccess() {
            // given
            String token = "Bearer test-token";

            // when
            String result = jwtUtil.substringToken(token);

            // then
            assertThat(result).isEqualTo("test-token");
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 추출 실패")
        void substringTokenFail() {
            // given
            String invalidToken = "Invalid-token";

            // when & then
            assertThrows(ServerException.class, () -> jwtUtil.substringToken(invalidToken));
        }

        @Test
        @DisplayName("쿠키에서 토큰 추출 성공")
        void getTokenFromRequestSuccess() {
            // given
            Cookie cookie = new Cookie("Authorization", "test-token");
            Cookie[] cookies = new Cookie[]{cookie};
            when(request.getCookies()).thenReturn(cookies);

            // when
            String token = jwtUtil.getTokenFromRequest(request);

            // then
            assertThat(token).isEqualTo("test-token");
        }
    }

    @Nested
    @DisplayName("쿠키 설정 테스트")
    class SetCookieTest {

        @Test
        @DisplayName("Access Token 쿠키 설정")
        void setAccessTokenCookie() {
            // given
            String token = "test-access-token";

            // when
            jwtUtil.setAccessTokenCookie(token);

            // then
            verify(response).addHeader(eq("Set-Cookie"), anyString());
            verify(response).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("Refresh Token 쿠키 설정")
        void setRefreshTokenCookie() {
            // given
            String token = "test-refresh-token";

            // when
            jwtUtil.setRefreshTokenCookie(token);

            // then
            verify(response).addHeader(eq("Set-Cookie"), anyString());
            verify(response, times(2)).addCookie(any(Cookie.class));
        }
    }
}
