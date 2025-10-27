package com.example.phonehub.auth.controller;

import com.example.phonehub.auth.service.AuthService;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API đăng nhập và đăng ký")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @Operation(summary = "🔐 Đăng nhập", description = "Đăng nhập và nhận JWT token")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đăng nhập",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "username": "john_doe",
                          "password": "password123"
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                ApiResponse<Map<String, String>> response = ApiResponse.badRequest("Username và password không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Call AuthService to signin
            Map<String, String> data = authService.signin(username, password);
            
            ApiResponse<Map<String, String>> response = ApiResponse.success("Đăng nhập thành công", data);
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error("Username hoặc password không đúng", 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error("Lỗi khi đăng nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "➕ Đăng ký", description = "Tạo tài khoản người dùng mới")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đăng ký",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "username": "newuser",
                          "password": "password123",
                          "email": "newuser@example.com"
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            String email = credentials.get("email");

            if (username == null || password == null) {
                ApiResponse<AuthResponse> response = ApiResponse.badRequest("Username và password không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Call AuthService to signup
            AuthResponse authResponse = authService.signup(username, password, email);
            
            ApiResponse<AuthResponse> response = ApiResponse.success("Đăng ký thành công", authResponse);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            ApiResponse<AuthResponse> response = ApiResponse.error("Lỗi khi đăng ký: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ApiResponse<AuthResponse> response = ApiResponse.error("Lỗi khi đăng ký: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🔄 Refresh Token", description = "Tạo token mới từ refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                ApiResponse<Map<String, String>> response = ApiResponse.badRequest("Refresh token không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Call AuthService to refresh token
            Map<String, String> data = authService.refreshToken(refreshToken);
            
            ApiResponse<Map<String, String>> response = ApiResponse.success("Refresh token thành công", data);
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error("Refresh token không hợp lệ", 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error("Lỗi khi refresh token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
