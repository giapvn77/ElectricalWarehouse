package com.electricalwarehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Mail mail, Bootstrap bootstrap, Alert alert,
                            String uploadDir, String pdfFont) {

    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Mail(boolean enabled, String from) {
    }

    public record Bootstrap(String adminUsername, String adminPassword) {
    }

    public record Alert(String cron, int expiryWarningDays) {
    }
}
