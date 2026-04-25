package com.yagubogu.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.oauth")
public record AdminOAuthProperties(
        OAuthClient google
) {

    public record OAuthClient(
            String clientId
    ) {
    }
}
