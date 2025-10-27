package com.example.phonehub.auth;

import com.example.phonehub.dto.AuthResponse;
import com.example.phonehub.entity.Role;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.RoleRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JwtUtil jwtUtils;
    
    /**
     * Đăng nhập user và tạo JWT tokens
     */
    @Transactional
    public Map<String, String> signin(String username, String password) {
        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        
        // Verify password với MD5
        if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        // Generate JWT token và Refresh Token
        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        // Lưu refresh token vào database
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        
        // Prepare response data
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        // data.put("refreshToken", refreshToken);
        data.put("type", "Bearer");
        data.put("username", username);
        data.put("userId", String.valueOf(user.getId()));
        data.put("email", user.getEmail());
        data.put("roleId", String.valueOf(user.getRole().getId()));
        data.put("roleName", user.getRole().getName());
        
        return data;
    }
    
    /**
     * Đăng ký user mới
     */
    @Transactional
    public AuthResponse signup(String username, String password, String email) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username đã tồn tại");
        }
        
        // Check if email already exists (if provided)
        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email đã tồn tại");
            }
        }
        
        // Get default role ID = 3
        Role defaultRole = roleRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Default role with ID 3 not found"));
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(PasswordUtils.encodeMD5(password)); // Encode password MD5
        newUser.setEmail(email);
        newUser.setRole(defaultRole);
        
        // Save user
        User savedUser = userRepository.save(newUser);
        
        // Return AuthResponse
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getPhone(),
            savedUser.getAvatar(),
            savedUser.getAddress(),
            savedUser.getRole().getName()
        );
    }
    
    /**
     * Refresh token và tạo tokens mới
     */
    @Transactional
    public Map<String, String> refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token không hợp lệ");
        }
        
        // Get username from refresh token
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        
        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        // Verify refresh token matches stored token
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token không hợp lệ");
        }
        
        // Generate new tokens
        String newToken = jwtUtils.generateToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        // Update refresh token in database
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        
        // Prepare response data
        Map<String, String> data = new HashMap<>();
        data.put("token", newToken);
        data.put("refreshToken", newRefreshToken);
        data.put("type", "Bearer");
        data.put("username", username);
        data.put("userId", String.valueOf(user.getId()));
        data.put("email", user.getEmail());
        data.put("roleId", String.valueOf(user.getRole().getId()));
        data.put("roleName", user.getRole().getName());
        
        return data;
    }
    
    /**
     * Verify token validity
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }
    
    /**
     * Get user info from token
     */
    public Map<String, String> getUserInfoFromToken(String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new BadCredentialsException("Token không hợp lệ");
        }
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", jwtUtils.getUsernameFromToken(token));
        userInfo.put("userId", String.valueOf(jwtUtils.getUserIdFromToken(token)));
        userInfo.put("email", jwtUtils.getEmailFromToken(token));
        
        // Get all claims
        var claims = jwtUtils.getAllClaimsFromToken(token);
        userInfo.put("phone", claims.get("phone", String.class));
        userInfo.put("avatar", claims.get("avatar", String.class));
        userInfo.put("address", claims.get("address", String.class));
        
        return userInfo;
    }
}

