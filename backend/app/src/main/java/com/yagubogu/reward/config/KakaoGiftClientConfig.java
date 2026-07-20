package com.yagubogu.reward.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(KakaoGiftProperties.class)
@Configuration
public class KakaoGiftClientConfig {

    @Bean
    public RestClient kakaoGiftRestClient(
            final KakaoGiftProperties properties,
            @Qualifier("kakaoGiftRequestFactory") final ClientHttpRequestFactory requestFactory
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory kakaoGiftRequestFactory(final KakaoGiftProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());
        return factory;
    }
}
