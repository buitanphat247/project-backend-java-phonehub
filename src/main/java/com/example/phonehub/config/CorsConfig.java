package com.example.phonehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép tất cả origins (dùng pattern để hỗ trợ cả http và https)
        // Pattern "*" sẽ match mọi origin
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Cho phép tất cả methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        // ⚠️ QUAN TRỌNG: Expose headers để frontend có thể đọc được
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-New-Access-Token",
                "X-New-Refresh-Token",
                "X-Token-Status",
                "Content-Type",
                "Content-Disposition"));

        // Set allowCredentials = false khi dùng "*" origins
        configuration.setAllowCredentials(false);

        // Cache preflight request trong 1 giờ
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
