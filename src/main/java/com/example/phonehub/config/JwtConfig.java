package com.example.phonehub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình JWT Token
 * Quản lý thời gian sống của Access Token và Refresh Token
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * Secret key để ký JWT token
     */
    private String secret;

    /**
     * Thời gian sống của Access Token (milliseconds)
     * Mặc định: 15 phút (900000 ms)
     */
    private long accessTokenExpiration = 900000; // 15 minutes

    /**
     * Thời gian sống của Refresh Token (milliseconds)
     * Mặc định: 7 ngày (604800000 ms)
     */
    private long refreshTokenExpiration = 604800000; // 7 days

    /**
     * Thời gian sống của Access Token cho development (milliseconds)
     * Mặc định: 1 giờ (3600000 ms)
     */
    private long devAccessTokenExpiration = 30000; // 1 hour

    /**
     * Thời gian sống của Refresh Token cho development (milliseconds)
     * Mặc định: 30 ngày (2592000000 ms)
     */
    private long devRefreshTokenExpiration = 604800000; // 30 days

    /**
     * Environment hiện tại (dev, prod, test)
     */
    private String environment = "dev";

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public long getDevAccessTokenExpiration() {
        return devAccessTokenExpiration;
    }

    public void setDevAccessTokenExpiration(long devAccessTokenExpiration) {
        this.devAccessTokenExpiration = devAccessTokenExpiration;
    }

    public long getDevRefreshTokenExpiration() {
        return devRefreshTokenExpiration;
    }

    public void setDevRefreshTokenExpiration(long devRefreshTokenExpiration) {
        this.devRefreshTokenExpiration = devRefreshTokenExpiration;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Lấy thời gian sống Access Token dựa trên environment
     */
    public long getEffectiveAccessTokenExpiration() {
        return "dev".equalsIgnoreCase(environment) ? devAccessTokenExpiration : accessTokenExpiration;
    }

    /**
     * Lấy thời gian sống Refresh Token dựa trên environment
     */
    public long getEffectiveRefreshTokenExpiration() {
        return "dev".equalsIgnoreCase(environment) ? devRefreshTokenExpiration : refreshTokenExpiration;
    }

    /**
     * Kiểm tra có phải môi trường development không
     */
    public boolean isDevelopment() {
        return "dev".equalsIgnoreCase(environment);
    }

    /**
     * Kiểm tra có phải môi trường production không
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(environment);
    }
}
