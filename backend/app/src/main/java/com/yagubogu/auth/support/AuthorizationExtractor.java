package com.yagubogu.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class AuthorizationExtractor {

    public static final String ADMIN_ACCESS_TOKEN_COOKIE = "admin_access_token";
    private static final String BEARER_PREFIX = "Bearer";

    public Optional<String> extract(final NativeWebRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(extractToken(header));
        }

        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        if (servletRequest != null) {
            return extractAdminAccessTokenCookie(servletRequest);
        }

        return Optional.empty();
    }

    public Optional<String> extract(final HttpServletRequest httpServletRequest) {
        Enumeration<String> headers = httpServletRequest.getHeaders(HttpHeaders.AUTHORIZATION);
        while (headers != null && headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header.startsWith(BEARER_PREFIX)) {
                return Optional.of(extractToken(header));
            }
        }

        return extractAdminAccessTokenCookie(httpServletRequest);
    }

    private Optional<String> extractAdminAccessTokenCookie(final HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> ADMIN_ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .map(cookie -> cookie.getValue())
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private String extractToken(final String header) {
        return header.substring(BEARER_PREFIX.length()).trim();
    }
}
