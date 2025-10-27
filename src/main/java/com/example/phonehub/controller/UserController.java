package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.RequiresAuth;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateUserRequest;
import com.example.phonehub.dto.UserDto;
import com.example.phonehub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "API quản lý người dùng")
public class UserController {

    @Autowired
    private UserService userService;

    @RequiresAuth(roles = {"ADMIN"})
    @Operation(summary = "📄 Lấy danh sách người dùng có phân trang", description = "Trả về danh sách người dùng với phân trang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng người dùng mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserDto> users = userService.getAllUsers(page, size);
            ApiResponse<Page<UserDto>> response = ApiResponse.success("Lấy danh sách người dùng thành công", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<UserDto>> response = ApiResponse
                    .error("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🔍 Lấy người dùng theo ID", description = "Trả về thông tin chi tiết của một người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy người dùng")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "ID của người dùng", required = true, example = "1") @PathVariable Integer id) {
        try {
            Optional<UserDto> user = userService.getUserById(id);
            if (user.isPresent()) {
                ApiResponse<UserDto> response = ApiResponse.success("Lấy người dùng thành công", user.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserDto> response = ApiResponse.notFound("Không tìm thấy người dùng với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi lấy người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🔎 Tìm người dùng theo username", description = "Tìm kiếm người dùng theo username")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy người dùng")
    })
    @GetMapping("/search/username")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(
            @Parameter(description = "Username", required = true, example = "john_doe") @RequestParam String username) {
        try {
            Optional<UserDto> user = userService.getUserByUsername(username);
            if (user.isPresent()) {
                ApiResponse<UserDto> response = ApiResponse.success("Tìm thấy người dùng", user.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserDto> response = ApiResponse
                        .notFound("Không tìm thấy người dùng với username: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi tìm người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "📧 Tìm người dùng theo email", description = "Tìm kiếm người dùng theo email")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy người dùng")
    })
    @GetMapping("/search/email")
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(
            @Parameter(description = "Email", required = true, example = "john@example.com") @RequestParam String email) {
        try {
            Optional<UserDto> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                ApiResponse<UserDto> response = ApiResponse.success("Tìm thấy người dùng", user.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserDto> response = ApiResponse.notFound("Không tìm thấy người dùng với email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi tìm người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "➕ Tạo người dùng mới", description = "Tạo một người dùng mới với role ID = 3 (cấp thấp nhất)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Tạo người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "❌ Username hoặc email đã tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin người dùng mới", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserRequest.class), examples = @ExampleObject(value = """
                    {
                      "username": "john_doe",
                      "password": "password123",
                      "email": "john@example.com",
                      "phone": "0123456789",
                      "address": "123 Main St",
                      "avatar": "https://example.com/avatar.jpg"
                    }
                    """))) @Valid @RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.createUser(request);
            ApiResponse<UserDto> response = ApiResponse.success("Tạo người dùng thành công", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                ApiResponse<UserDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi tạo người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi tạo người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "✏️ Cập nhật người dùng", description = "Cập nhật thông tin của một người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "❌ Username hoặc email đã tồn tại")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "ID của người dùng", required = true, example = "1") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin cập nhật người dùng", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserRequest.class), examples = @ExampleObject(value = """
                    {
                      "username": "john_doe_updated",
                      "password": "newpassword123",
                      "email": "john.updated@example.com",
                      "phone": "0987654321",
                      "address": "456 Updated St",
                      "avatar": "https://example.com/new-avatar.jpg"
                    }
                    """))) @Valid @RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.updateUser(id, request);
            ApiResponse<UserDto> response = ApiResponse.success("Cập nhật người dùng thành công", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<UserDto> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (e.getMessage().contains("already exists")) {
                ApiResponse<UserDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi cập nhật người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<UserDto> response = ApiResponse.error("Lỗi khi cập nhật người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🗑️ Xóa người dùng", description = "Xóa một người dùng khỏi hệ thống")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy người dùng")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "ID của người dùng", required = true, example = "1") @PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            ApiResponse<Void> response = ApiResponse.success("Xóa người dùng thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<Void> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🔐 Xác thực mật khẩu", description = "Kiểm tra username và password có đúng không")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Xác thực thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "❌ Username hoặc password không đúng")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin xác thực", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "username": "john_doe",
                      "password": "password123"
                    }
                    """))) @RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                ApiResponse<Boolean> response = ApiResponse.badRequest("Username và password không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean isValid = userService.verifyUserPassword(username, password);

            if (isValid) {
                ApiResponse<Boolean> response = ApiResponse.success("Xác thực thành công", true);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Boolean> response = ApiResponse.error("Username hoặc password không đúng", 401);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            ApiResponse<Boolean> response = ApiResponse.error("Lỗi khi xác thực: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
