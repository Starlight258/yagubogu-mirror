package com.yagubogu.admin.controller;

import com.yagubogu.admin.dto.AdminLoginRequest;
import com.yagubogu.admin.config.AdminOAuthProperties;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.service.AuthService;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.AuthorizationExtractor;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminWebController {

    private static final String LOGIN_VIEW = "admin/login";
    private static final String DASHBOARD_VIEW = "admin/dashboard";
    private static final int ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 15;
    private static final int DELETE_COOKIE_MAX_AGE_SECONDS = 0;

    private final AuthService authService;
    private final AuthTokenProvider authTokenProvider;
    private final AdminOAuthProperties adminOAuthProperties;

    @GetMapping({"", "/"})
    public String dashboard(
            @CookieValue(name = AuthorizationExtractor.ADMIN_ACCESS_TOKEN_COOKIE, required = false) final String accessToken
    ) {
        if (!hasAdminPermission(accessToken)) {
            return "redirect:/admin/login";
        }

        return DASHBOARD_VIEW;
    }

    @GetMapping("/login")
    public String login(
            @CookieValue(name = AuthorizationExtractor.ADMIN_ACCESS_TOKEN_COOKIE, required = false) final String accessToken,
            final Model model
    ) {
        if (hasAdminPermission(accessToken)) {
            return "redirect:/admin";
        }

        model.addAttribute("googleClientId", adminOAuthProperties.google().clientId());
        model.addAttribute("appleClientId", adminOAuthProperties.apple().clientId());

        return LOGIN_VIEW;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody final AdminLoginRequest request,
            final HttpServletRequest servletRequest,
            final HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.login(new LoginParam(request.idToken(), request.provider()));
        validateAdmin(loginResponse.accessToken());

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createAdminAccessTokenCookie(
                                loginResponse.accessToken(),
                                ACCESS_TOKEN_COOKIE_MAX_AGE_SECONDS,
                                servletRequest.isSecure()
                        )
                        .toString()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createAdminAccessTokenCookie("", DELETE_COOKIE_MAX_AGE_SECONDS, request.isSecure()).toString()
        );

        return ResponseEntity.noContent().build();
    }

    private boolean hasAdminPermission(final String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        try {
            return authTokenProvider.getRoleByAccessToken(accessToken).hasPermission(Role.ADMIN);
        } catch (UnAuthorizedException exception) {
            return false;
        }
    }

    private void validateAdmin(final String accessToken) {
        Role role = authTokenProvider.getRoleByAccessToken(accessToken);
        if (!role.hasPermission(Role.ADMIN)) {
            throw new ForbiddenException("Admin permission required");
        }
    }

    private ResponseCookie createAdminAccessTokenCookie(
            final String accessToken,
            final int maxAgeSeconds,
            final boolean secure
    ) {
        return ResponseCookie.from(AuthorizationExtractor.ADMIN_ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

}
