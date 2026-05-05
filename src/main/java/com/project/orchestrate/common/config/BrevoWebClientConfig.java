package com.project.orchestrate.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BrevoWebClientConfig {

    @Value("${brevo.base-url}")
    String brevoBaseUrl;

    @Value("${brevo.api-key}")
    String brevoApiKey;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(brevoBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", brevoApiKey)
                .build();
    }
}
