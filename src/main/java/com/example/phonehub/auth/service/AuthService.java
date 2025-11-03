package com.example.phonehub.auth.service;

import com.example.phonehub.auth.util.JwtUtil;
import com.example.phonehub.dto.AuthResponse;
import com.example.phonehub.entity.Role;
import com.example.phonehub.entity.User;
import com.example.phonehub.dto.UploadResponse;
import com.example.phonehub.repository.RoleRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.UploadService;
import com.example.phonehub.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.text.Normalizer;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtils;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Autowired
    private UploadService uploadService;

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
                savedUser.getRole().getName());
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
     * Đăng nhập bằng Google id_token: xác minh, provision user, phát hành JWT
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, String> googleSignin(String idToken) {
        // Gọi endpoint tokeninfo của Google để xác minh chữ ký và payload của id_token
        // Lưu ý: Có thể thay bằng GoogleIdTokenVerifier để không phụ thuộc HTTP tokeninfo
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        RestTemplate rest = new RestTemplate();
        Map<String, Object> tokenInfo;
        try {
            tokenInfo = rest.getForObject(url, Map.class);
        } catch (RestClientException e) {
            throw new BadCredentialsException("Không xác thực được id_token");
        }

        // Bắt buộc phải có aud (clientId) và email để định danh người dùng
        if (tokenInfo == null || tokenInfo.get("aud") == null || tokenInfo.get("email") == null) {
            throw new BadCredentialsException("id_token không hợp lệ");
        }

        // Kiểm tra aud khớp clientId cấu hình của dự án nhằm ngăn id_token phát hành cho ứng dụng khác
        if (googleClientId != null && !googleClientId.isBlank()) {
            String aud = String.valueOf(tokenInfo.get("aud"));
            if (!googleClientId.equals(aud)) {
                throw new BadCredentialsException("aud không khớp");
            }
        }

        // Một số id_token có cờ email_verified; nếu có và false thì từ chối
        Object emailVerified = tokenInfo.get("email_verified");
        if (emailVerified != null && !Boolean.parseBoolean(String.valueOf(emailVerified))) {
            throw new BadCredentialsException("Email chưa xác minh");
        }

        // Trích xuất các trường cần thiết từ id_token
        String email = String.valueOf(tokenInfo.get("email"));
        String sub = String.valueOf(tokenInfo.get("sub"));
        String picture = String.valueOf(tokenInfo.getOrDefault("picture", ""));
        String name = String.valueOf(tokenInfo.getOrDefault("name", ""));

        // Tìm người dùng theo email; nếu chưa có thì provision user mới với role mặc định (id=3)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role defaultRole = roleRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("Default role with ID 3 not found"));
            User u = new User();
            String baseUsername = name == null || name.isBlank() ? ("gg_" + sub) : normalizeUsername(name);
            String finalUsername = baseUsername;
            if (userRepository.existsByUsername(finalUsername)) {
                String suffix = sub != null && sub.length() > 6 ? sub.substring(sub.length() - 6) : UUID.randomUUID().toString().substring(0, 6);
                finalUsername = baseUsername + "_" + suffix;
            }
            u.setUsername(finalUsername);
            u.setPassword(PasswordUtils.encodeMD5(UUID.randomUUID().toString()));
            u.setEmail(email);
            if (picture != null && !picture.isBlank()) {
                try {
                    UploadResponse up = uploadService.uploadFromUrl(picture);
                    u.setAvatar(up.getFileUrl());
                } catch (Exception ignored) { u.setAvatar(picture); }
            }
            u.setRole(defaultRole);
            return userRepository.save(u);
        });

        // Đồng bộ avatar nếu user cũ chưa có ảnh
        if ((user.getAvatar() == null || user.getAvatar().isBlank()) && picture != null && !picture.isBlank()) {
            try {
                UploadResponse up = uploadService.uploadFromUrl(picture);
                user.setAvatar(up.getFileUrl());
                userRepository.save(user);
            } catch (Exception ignored) { }
        }

        // Phát hành accessToken + refreshToken theo cơ chế JWT nội bộ và lưu refreshToken vào DB
        String token = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Chuẩn hóa dữ liệu phản hồi theo format các API đăng nhập hiện có
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("type", "Bearer");
        data.put("username", user.getUsername());
        data.put("userId", String.valueOf(user.getId()));
        data.put("email", user.getEmail());
        data.put("roleId", String.valueOf(user.getRole().getId()));
        data.put("roleName", user.getRole().getName());
        return data;
    }

    // Chuẩn hóa tên thành username: bỏ dấu, chữ thường, bỏ khoảng trắng/ký tự không [a-z0-9_]
    private String normalizeUsername(String input) {
        String noDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String lowered = noDiacritics.toLowerCase().replaceAll("[\\s]+", "");
        String cleaned = lowered.replaceAll("[^a-z0-9_]", "");
        return cleaned.isBlank() ? "user" + UUID.randomUUID().toString().substring(0, 6) : cleaned;
    }

}
