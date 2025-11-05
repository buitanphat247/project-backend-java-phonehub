package com.example.phonehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Cấu hình CORS cho Spring MVC (WebMvcConfigurer)
    // Lưu ý: CorsFilter đã xử lý CORS ở tầng filter, nhưng cấu hình này vẫn cần cho các trường hợp khác
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Cho phép tất cả origins (pattern) - bao gồm Vercel, localhost, IP addresses
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*") // Cho phép tất cả headers
                .exposedHeaders("Authorization", "X-New-Access-Token", "X-New-Refresh-Token", 
                        "X-Token-Status", "Content-Type", "Content-Disposition")
                .allowCredentials(false) // false khi dùng "*" origin
                .maxAge(3600L); // Cache preflight trong 1 giờ
    }

    // Cấu hình CORS cho Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Dùng pattern để cho phép tất cả origins (bao gồm Vercel)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-New-Access-Token",
                "X-New-Refresh-Token",
                "X-Token-Status",
                "Content-Type",
                "Content-Disposition"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
