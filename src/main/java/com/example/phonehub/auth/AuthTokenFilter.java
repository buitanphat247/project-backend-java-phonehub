package com.example.phonehub.auth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.phonehub.auth.service.CustomUserDetailsService;
import com.example.phonehub.auth.util.JwtUtil;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtils;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                // Check if token is expired
                if (jwtUtils.isTokenExpired(jwt)) {
                    // Token expired, try to get refresh token from database
                    try {
                        // Get user ID from expired token (can still read claims)
                        Integer userId = jwtUtils.getUserIdFromExpiredToken(jwt);
                        
                        // Get user from database with role (eager fetch to avoid LazyInitializationException)
                        Optional<User> userOptional = userRepository.findByIdWithRole(userId);
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            String refreshToken = user.getRefreshToken();
                            
                            // Check if refresh token exists and is valid
                            if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken)) {
                                // Refresh token is valid, generate new access token
                                String newAccessToken = jwtUtils.generateToken(user);
                                
                                // Set new access token in response header for client to pick up
                                response.setHeader("X-New-Access-Token", newAccessToken);
                                
                                // Continue with authentication using new token
                                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            } else {
                                // Refresh token is invalid or expired
                                response.setHeader("X-Token-Status", "REFRESH_TOKEN_EXPIRED");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Access token expired and refresh token is invalid or expired. Please login again.\"}");
                                return;
                            }
                        } else {
                            // User not found
                            response.setHeader("X-Token-Status", "USER_NOT_FOUND");
                        }
                    } catch (ExpiredJwtException e) {
                        // Can still read claims from expired token, but if this fails, token is invalid
                        System.out.println("Cannot read claims from expired token: " + e.getMessage());
                    }
                } else if (jwtUtils.validateJwtToken(jwt)) {
                    // Token is valid, set authentication
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e);
        }
        filterChain.doFilter(request, response);
    }
    
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}