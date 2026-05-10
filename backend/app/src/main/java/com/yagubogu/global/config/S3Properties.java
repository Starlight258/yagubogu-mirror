package com.yagubogu.global.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String bucket,
        Duration presignExpiration,
        String endpoint,
        String region,
        String publicBaseUrl,
        String defaultProfileImageUrl
) {

    public String objectUrl(final String key) {
        return trimTrailingSlash(resolvePublicBaseUrl()) + "/" + trimLeadingSlash(key);
    }

    private String resolvePublicBaseUrl() {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl;
        }
        return trimTrailingSlash(endpoint) + "/" + trimSlashes(bucket);
    }

    private static String trimTrailingSlash(final String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String trimLeadingSlash(final String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int start = 0;
        while (start < value.length() && value.charAt(start) == '/') {
            start++;
        }
        return value.substring(start);
    }

    private static String trimSlashes(final String value) {
        return trimTrailingSlash(trimLeadingSlash(value));
    }
}
