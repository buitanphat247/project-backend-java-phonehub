package com.example.phonehub.auth.interceptor;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.auth.annotation.RequiresAuth;
import com.example.phonehub.auth.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor để kiểm tra role-based access control
 */
@Component
public class RoleBasedAccessInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Chỉ xử lý cho method handlers
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Check for @Public annotation - skip authentication
        boolean isPublicMethod = handlerMethod.getMethod().isAnnotationPresent(Public.class);
        boolean isPublicClass = handlerMethod.getBeanType().isAnnotationPresent(Public.class);
        
        // If method or class is @Public, skip authentication
        if (isPublicMethod || isPublicClass) {
            return true;
        }
        
        // Kiểm tra annotation @RequiresAuth
        RequiresAuth requiresAuth = handlerMethod.getMethodAnnotation(RequiresAuth.class);
        if (requiresAuth == null) {
            // Kiểm tra class level annotation
            requiresAuth = handlerMethod.getBeanType().getAnnotation(RequiresAuth.class);
        }
        
        // Nếu không có @RequiresAuth, không cần kiểm tra role
        if (requiresAuth == null) {
            return true;
        }
        
        // Lấy token từ request
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized: Token is required\"}");
            return false;
        }
        
        String token = authHeader.substring(7);
        
        // Validate token
        if (!jwtUtil.validateJwtToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized: Invalid token\"}");
            return false;
        }
        
        // Kiểm tra roles nếu có yêu cầu
        String[] requiredRoles = requiresAuth.roles();
        if (requiredRoles != null && requiredRoles.length > 0) {
            String userRole = jwtUtil.getRoleNameFromToken(token);
            
            // Check if user has required role
            boolean hasRequiredRole = false;
            for (String role : requiredRoles) {
                if (role.equalsIgnoreCase(userRole)) {
                    hasRequiredRole = true;
                    break;
                }
            }
            
            if (!hasRequiredRole) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Forbidden: Insufficient permissions\"}");
                return false;
            }
        }
        
        return true;
    }
}

