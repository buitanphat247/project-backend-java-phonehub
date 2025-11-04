package com.example.phonehub.auth.controller;

import com.example.phonehub.auth.annotation.Public;
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
import com.example.phonehub.dto.ChangeEmailRequest;
import com.example.phonehub.service.EmailVerificationService;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Public
@Tag(name = "Authentication", description = "API đăng nhập và đăng ký")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private EmailVerificationService emailVerificationService;

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
                          "email": "newuser@example.com",
                          "phone": "0123456789",
                          "address": "123 Main Street",
                          "birthday": "2000-11-01"
                        }
                        """)
                )
            )
            @RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            String email = credentials.get("email");
            String phone = credentials.get("phone");
            String address = credentials.get("address");
            String birthdayStr = credentials.get("birthday");

            if (username == null || password == null) {
                ApiResponse<AuthResponse> response = ApiResponse.badRequest("Username và password không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Parse birthday từ string (format: "2000-11-01")
            LocalDate birthday = null;
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                try {
                    birthday = LocalDate.parse(birthdayStr);
                } catch (Exception e) {
                    ApiResponse<AuthResponse> response = ApiResponse.badRequest("Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng định dạng yyyy-MM-dd");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            // Call AuthService to signup
            AuthResponse authResponse = authService.signup(username, password, email, phone, address, birthday);
            
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

    @Operation(summary = "🔑 Đăng nhập Google", description = "Nhận id_token (chuỗi) từ frontend, xác thực và phát hành JWT")
    @PostMapping("/signin/google")
    public ResponseEntity<ApiResponse<Map<String, String>>> googleSignin(@RequestBody String idToken) {
        try {
            if (idToken != null) {
                idToken = idToken.trim();
                if (idToken.startsWith("\"") && idToken.endsWith("\"")) {
                    idToken = idToken.substring(1, idToken.length() - 1);
                }
            }
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.badRequest("Thiếu idToken"));
            }
            Map<String, String> data = authService.googleSignin(idToken);
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập Google thành công", data));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("id_token không hợp lệ", 401));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi đăng nhập Google: " + e.getMessage()));
        }
    }

    @Operation(summary = "📧 Yêu cầu đổi email", description = "Sinh token xác minh và gửi email tới địa chỉ mới")
    @PostMapping("/change-email-request")
    public ResponseEntity<ApiResponse<Boolean>> changeEmailRequest(@Valid @RequestBody ChangeEmailRequest request) {
        try {
            emailVerificationService.createEmailVerificationToken(request);
            return ResponseEntity.ok(ApiResponse.success("Đã gửi email xác minh đến địa chỉ mới", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi tạo yêu cầu đổi email: " + e.getMessage()));
        }
    }

    @Operation(summary = "✅ Xác minh đổi email", description = "Xác thực token và cập nhật email người dùng")
    @GetMapping("/verify-email-change")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmailChange(@RequestParam("token") String token) {
        try {
            emailVerificationService.verifyEmailToken(token);
            return ResponseEntity.ok(ApiResponse.success("Xác minh email thành công", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Invalid or expired token", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi xác minh email: " + e.getMessage()));
        }
    }
}
