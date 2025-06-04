package com.webstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class WebStoreSpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(WebStoreSpringBootApp.class, args);
    }
}
