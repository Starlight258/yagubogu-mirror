package com.yagubogu.admin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@EnableConfigurationProperties(CrawlingClientProperties.class)
@Configuration
public class CrawlingClientConfig {

    private final CrawlingClientProperties properties;

    @Bean
    public RestClient crawlingRestClient(final ClientHttpRequestFactory crawlingClientHttpRequestFactory) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(crawlingClientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory crawlingClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());

        return factory;
    }
}
