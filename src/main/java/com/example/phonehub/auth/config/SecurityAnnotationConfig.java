package com.example.phonehub.auth.config;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.auth.annotation.RequiresAuth;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration để tự động phát hiện public và protected routes
 * từ @Public và @RequiresAuth annotations
 */
@Configuration
public class SecurityAnnotationConfig {
    
    private final ApplicationContext applicationContext;
    
    public SecurityAnnotationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Lấy danh sách public URLs từ annotations @Public
     */
    @Bean
    public Set<String> publicUrls() {
        Set<String> urls = new HashSet<>();
        
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
        
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            RequestMappingInfo mappingInfo = entry.getKey();
            
            // Check method level annotation
            boolean isPublicMethod = handlerMethod.getMethod().isAnnotationPresent(Public.class);
            
            // Check class level annotation
            boolean isPublicClass = handlerMethod.getBeanType().isAnnotationPresent(Public.class);
            
            // Check method level @RequiresAuth
            boolean requiresAuthMethod = handlerMethod.getMethod().isAnnotationPresent(RequiresAuth.class);
            
            // Method level annotation overrides class level
            // If method has @RequiresAuth, it's not public
            // If method has @Public, it's public regardless of class
            boolean isPublic = (isPublicMethod || isPublicClass) && !requiresAuthMethod;
            
            if (isPublic && mappingInfo.getPatternValues() != null) {
                Set<String> patterns = mappingInfo.getPatternValues();
                urls.addAll(patterns);
                System.out.println("✅ Public URL detected: " + patterns);
            }
        }
        
        System.out.println("📋 Total public URLs: " + urls.size());
        System.out.println("🌐 Public URLs: " + urls);
        
        return urls;
    }
}

