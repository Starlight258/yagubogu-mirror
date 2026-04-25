package com.yagubogu.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(AdminOAuthProperties.class)
@Configuration
public class AdminWebConfig {
}
