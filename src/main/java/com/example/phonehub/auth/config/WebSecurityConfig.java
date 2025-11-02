package com.example.phonehub.auth.config;

import com.example.phonehub.auth.AuthEntryPointJwt;
import com.example.phonehub.auth.AuthTokenFilter;
import com.example.phonehub.auth.interceptor.RoleBasedAccessInterceptor;
import com.example.phonehub.config.CorsConfig;
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
                    // Always public endpoints
                    auth.requestMatchers("/api/v1/auth/**").permitAll();
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll();
                    auth.requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll();
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/actuator/**").permitAll();
                    
                    // Dynamic public URLs from @Public annotation
                    // Group URLs by base path to handle path variables
                    java.util.Set<String> basePaths = new java.util.HashSet<>();
                    for (String url : publicUrls) {
                        // Add the exact URL pattern
                        auth.requestMatchers(url).permitAll();
                        // Extract base path with wildcard for path variables (e.g., /api/v1/users/** from /api/v1/users/{id})
                        String basePath = url.replaceAll("/\\{[^}]+\\}", "/**");
                        if (!basePath.equals(url) && !basePaths.contains(basePath)) {
                            basePaths.add(basePath);
                            auth.requestMatchers(basePath).permitAll();
                        }
                        // Also add base path without trailing /** if needed
                        String basePathNoWildcard = url.replaceAll("/\\{[^}]+\\}", "");
                        if (!basePathNoWildcard.equals(url) && !basePathNoWildcard.equals(basePath) && 
                            !basePaths.contains(basePathNoWildcard + "/**")) {
                            basePaths.add(basePathNoWildcard + "/**");
                            auth.requestMatchers(basePathNoWildcard + "/**").permitAll();
                        }
                    }
                    
                    // All other requests require authentication
                    auth.anyRequest().authenticated();
                });
        
        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // Register interceptor for role-based access control
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleBasedAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/auth/**", "/swagger-ui/**", "/api-docs/**", "/");
    }
}
