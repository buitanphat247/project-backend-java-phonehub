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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Cho phép tất cả origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-New-Access-Token", "X-New-Refresh-Token", "X-Token-Status", "Content-Type", "Content-Disposition")
                .allowCredentials(false) // Nếu không dùng cookies, set false để tránh conflict với "*"
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép tất cả origins (nếu cần cụ thể thì thay bằng List.of("http://localhost:3000", "http://localhost:5173"))
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Cho phép tất cả methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        
        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ⚠️ QUAN TRỌNG: Expose headers để frontend có thể đọc được
        // Đây là header chứa Access-Control-Expose-Headers trong response
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "X-New-Access-Token",      // 👈 Token mới khi refresh
            "X-New-Refresh-Token",     // 👈 Refresh token mới
            "X-Token-Status",          // 👈 Trạng thái token (expired, invalid...)
            "Content-Type",
            "Content-Disposition"
        ));
        
        // Set allowCredentials = false khi dùng "*" origins (tránh conflict)
        // Nếu cần credentials, phải chỉ định cụ thể origins
        configuration.setAllowCredentials(false);
        
        // Cache preflight request trong 1 giờ
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

