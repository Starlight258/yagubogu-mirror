package com.yagubogu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class S3PropertiesTest {

    @DisplayName("publicBaseUrl 기준으로 객체 URL을 만든다")
    @Test
    void objectUrl_usesPublicBaseUrl() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com",
                "auto",
                "https://test-account.r2.cloudflarestorage.com/yagubogu/",
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png"
        );

        String objectUrl = properties.objectUrl("/images/profiles/abc-123");

        assertThat(objectUrl)
                .isEqualTo("https://test-account.r2.cloudflarestorage.com/yagubogu/images/profiles/abc-123");
    }

    @DisplayName("publicBaseUrl이 없으면 endpoint와 bucket으로 객체 URL을 만든다")
    @Test
    void objectUrl_fallbackToEndpointAndBucket() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com/",
                "auto",
                null,
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png"
        );

        String objectUrl = properties.objectUrl("images/profiles/abc-123");

        assertThat(objectUrl)
                .isEqualTo("https://test-account.r2.cloudflarestorage.com/yagubogu/images/profiles/abc-123");
    }
}
