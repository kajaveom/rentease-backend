package com.rentease.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppConfig {

    private Jwt jwt = new Jwt();
    private Cloudinary cloudinary = new Cloudinary();
    private Stripe stripe = new Stripe();
    private String frontendUrl;
    private int serviceFeePercent;

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
    }

    @Getter
    @Setter
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }

    @Getter
    @Setter
    public static class Stripe {
        private String secretKey;
        private String webhookSecret;
    }
}
