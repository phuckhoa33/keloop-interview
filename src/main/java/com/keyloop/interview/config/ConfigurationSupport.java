package com.keyloop.interview.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ JwtProperties.class, AppProperties.class })
public class ConfigurationSupport {
}
