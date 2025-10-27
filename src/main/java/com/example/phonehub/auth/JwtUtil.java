package com.example.phonehub.auth;

import com.example.phonehub.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import com.example.phonehub.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtUtil {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    private SecretKey key;
    
    // Initializes the key after the class is instantiated and the jwtSecret is injected, 
    // preventing the repeated creation of the key and enhancing performance
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    // Generate JWT token with user info
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("phone", user.getPhone())
                .claim("avatar", user.getAvatar())
                .claim("address", user.getAddress())
                .claim("roleId", user.getRole() != null ? user.getRole().getId() : null)
                .claim("roleName", user.getRole() != null ? user.getRole().getName() : null)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtConfig.getEffectiveAccessTokenExpiration()))
                .signWith(key)
                .compact();
    }
    
    // Generate Refresh Token
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtConfig.getEffectiveRefreshTokenExpiration()))
                .signWith(key)
                .compact();
    }
    // Get username from JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    // Get all claims from JWT token
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // Get user ID from token
    public Integer getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("id", Integer.class);
    }
    
    // Get email from token
    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("email", String.class);
    }
    
    // Get role ID from token
    public Integer getRoleIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("roleId", Integer.class);
    }
    
    // Get role name from token
    public String getRoleNameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("roleName", String.class);
    }
    
    // Validate JWT token
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}