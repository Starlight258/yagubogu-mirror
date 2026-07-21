package com.yagubogu.reward.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.gift")
public record KakaoGiftProperties(
        String baseUrl,
        String apiKey,
        String templateToken,
        Duration connectTimeout,
        Duration readTimeout
) {

    public KakaoGiftProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://gateway-giftbiz.kakao.com/openapi/giftbiz";
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(3);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
