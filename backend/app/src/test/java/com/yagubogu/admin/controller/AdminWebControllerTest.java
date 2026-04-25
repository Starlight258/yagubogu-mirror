package com.yagubogu.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.yagubogu.admin.dto.AdminLoginRequest;
import com.yagubogu.admin.config.AdminOAuthProperties;
import com.yagubogu.admin.config.AdminOAuthProperties.OAuthClient;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.service.AuthService;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminWebControllerTest {

    private static final String ACCESS_TOKEN = "access.token";

    private final AuthService authService = org.mockito.Mockito.mock(AuthService.class);
    private final AuthTokenProvider authTokenProvider = org.mockito.Mockito.mock(AuthTokenProvider.class);
    private final AdminWebController adminWebController = new AdminWebController(
            authService,
            authTokenProvider,
            new AdminOAuthProperties(new OAuthClient("admin-google-client-id"), new OAuthClient("admin-apple-client-id"))
    );

    @DisplayName("어드민 토큰이 없으면 로그인 페이지로 이동한다")
    @Test
    void dashboard_redirect_login() {
        // when
        String viewName = adminWebController.dashboard(null);

        // then
        assertThat(viewName).isEqualTo("redirect:/admin/login");
    }

    @DisplayName("어드민 토큰이 있으면 대시보드를 보여준다")
    @Test
    void dashboard() {
        // given
        given(authTokenProvider.getRoleByAccessToken(ACCESS_TOKEN)).willReturn(Role.ADMIN);

        // when
        String viewName = adminWebController.dashboard(ACCESS_TOKEN);

        // then
        assertThat(viewName).isEqualTo("admin/dashboard");
    }

    @DisplayName("어드민 로그인 성공 시 접근 토큰 쿠키를 발급한다")
    @Test
    void login_admin() {
        // given
        given(authService.login(any())).willReturn(new LoginResponse(ACCESS_TOKEN, "refresh.token", false, null));
        given(authTokenProvider.getRoleByAccessToken(ACCESS_TOKEN)).willReturn(Role.ADMIN);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // when
        adminWebController.login(
                new AdminLoginRequest("id-token", OAuthProvider.GOOGLE),
                servletRequest,
                servletResponse
        );

        // then
        String cookie = servletResponse.getHeader("Set-Cookie");
        assertThat(cookie).contains("admin_access_token=" + ACCESS_TOKEN);
        assertThat(cookie).contains("HttpOnly");
        assertThat(cookie).contains("SameSite=Strict");
        assertThat(cookie).contains("Max-Age=900");
    }

    @DisplayName("어드민 권한이 아니면 로그인을 거부한다")
    @Test
    void login_user_forbidden() {
        // given
        given(authService.login(any())).willReturn(new LoginResponse(ACCESS_TOKEN, "refresh.token", false, null));
        given(authTokenProvider.getRoleByAccessToken(ACCESS_TOKEN)).willReturn(Role.USER);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // when & then
        assertThatThrownBy(() -> adminWebController.login(
                new AdminLoginRequest("id-token", OAuthProvider.GOOGLE),
                servletRequest,
                servletResponse
        )).isExactlyInstanceOf(ForbiddenException.class);
    }
}
