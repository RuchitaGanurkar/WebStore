package com.webstore.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "whatsapp")
@Data
public class WhatsAppConfiguration {

    private Webhook webhook = new Webhook();
    private Api api = new Api();

    @Data
    public static class Webhook {
        private String verifyToken;
    }

    @Data
    public static class Api {
        private String accessToken;
        private String version;
        private String baseUrl;
        private String phoneNumberId;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}