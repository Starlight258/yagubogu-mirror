package com.yagubogu.admin.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.crawling")
public record CrawlingClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    public CrawlingClientProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8081";
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(5);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(180);
        }
    }
}
