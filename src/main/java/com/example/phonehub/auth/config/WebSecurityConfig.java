package com.example.phonehub.auth.config;

import com.example.phonehub.auth.AuthEntryPointJwt;
import com.example.phonehub.auth.AuthTokenFilter;
import com.example.phonehub.auth.interceptor.RoleBasedAccessInterceptor;
import com.example.phonehub.config.CorsConfig;
import com.example.phonehub.utils.PerformanceLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    
    @Autowired
    private AuthTokenFilter authenticationJwtTokenFilter;
    
    @Autowired
    private SecurityAnnotationConfig securityAnnotationConfig;
    
    @Autowired
    private RoleBasedAccessInterceptor roleBasedAccessInterceptor;
    
    @Autowired
    private PerformanceLoggingInterceptor performanceLoggingInterceptor;
    
    @Autowired
    private CorsConfig corsConfig;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Get public URLs from @Public annotation
        Set<String> publicUrls = securityAnnotationConfig.publicUrls();
        
        http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Allow OPTIONS requests for CORS preflight
                    auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();
                    // Always public endpoints
                    auth.requestMatchers("/api/v1/auth/**").permitAll();
                    auth.requestMatchers("/api/v1/database/**").permitAll();
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll();
                    auth.requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll();
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/actuator/**").permitAll();
                    
                    // Dynamic public URLs from @Public annotation
                    // Spring Security automatically handles path variables like {id}, {productId}, etc.
                    // So we only need to add the exact patterns from annotations
                    java.util.Set<String> processedPatterns = new java.util.HashSet<>();
                    for (String url : publicUrls) {
                        if (url == null || url.isEmpty()) continue;
                        
                        // Skip if pattern already contains ** to avoid duplicate processing
                        if (url.contains("**")) continue;
                        
                        // Add the exact URL pattern (Spring handles path variables like {id} automatically)
                        if (!processedPatterns.contains(url)) {
                            processedPatterns.add(url);
                            auth.requestMatchers(url).permitAll();
                        }
                    }
                    
                    // All other requests require authentication
                    auth.anyRequest().authenticated();
                });
        
        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // Register interceptors
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Performance logging interceptor (chạy đầu tiên để đo thời gian)
        registry.addInterceptor(performanceLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/api-docs/**");
        
        // Role-based access control interceptor
        registry.addInterceptor(roleBasedAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/database/**", "/swagger-ui/**", "/api-docs/**", "/", "/actuator/**");
    }
}
