package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateRoleRequest;
import com.example.phonehub.dto.RoleDto;
import com.example.phonehub.service.RoleService;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/roles")
@Public
@Tag(name = "Role Management", description = "API quản lý vai trò người dùng")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(
        summary = "📄 Lấy danh sách vai trò có phân trang", 
        description = "Trả về danh sách vai trò với phân trang"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "✅ Lấy danh sách thành công"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoleDto>>> getAllRolesPaged(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng vai trò mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<RoleDto> roles = roleService.getAllRoles(page, size);
            ApiResponse<Page<RoleDto>> response = ApiResponse.success("Lấy danh sách vai trò thành công", roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<RoleDto>> response = ApiResponse.error("Lỗi khi lấy danh sách vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "🔍 Lấy vai trò theo ID", 
        description = "Trả về thông tin chi tiết của một vai trò"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "✅ Tìm thấy vai trò"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "❌ Không tìm thấy vai trò"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(
            @Parameter(description = "ID của vai trò", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            Optional<RoleDto> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                ApiResponse<RoleDto> response = ApiResponse.success("Lấy vai trò thành công", role.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<RoleDto> response = ApiResponse.notFound("Không tìm thấy vai trò với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi lấy vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "🔎 Tìm vai trò theo tên", 
        description = "Tìm kiếm vai trò theo tên"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "✅ Tìm thấy vai trò"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "❌ Không tìm thấy vai trò"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleByName(
            @Parameter(description = "Tên vai trò", required = true, example = "ADMIN")
            @RequestParam String name) {
        try {
            Optional<RoleDto> role = roleService.getRoleByName(name);
            if (role.isPresent()) {
                ApiResponse<RoleDto> response = ApiResponse.success("Tìm thấy vai trò", role.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<RoleDto> response = ApiResponse.notFound("Không tìm thấy vai trò với tên: " + name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi tìm vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "➕ Tạo vai trò mới", 
        description = "Tạo một vai trò mới trong hệ thống"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201", 
                description = "✅ Tạo vai trò thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", 
                description = "❌ Dữ liệu không hợp lệ"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409", 
                description = "❌ Tên vai trò đã tồn tại"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin vai trò mới",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateRoleRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "MODERATOR"
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleDto role = roleService.createRole(request);
            ApiResponse<RoleDto> response = ApiResponse.success("Tạo vai trò thành công", role);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                ApiResponse<RoleDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi tạo vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi tạo vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "✏️ Cập nhật vai trò", 
        description = "Cập nhật thông tin của một vai trò"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "✅ Cập nhật thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "❌ Không tìm thấy vai trò"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409", 
                description = "❌ Tên vai trò đã tồn tại"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            @Parameter(description = "ID của vai trò", required = true, example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cập nhật vai trò",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateRoleRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "SUPER_ADMIN"
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateRoleRequest request) {
        try {
            RoleDto role = roleService.updateRole(id, request);
            ApiResponse<RoleDto> response = ApiResponse.success("Cập nhật vai trò thành công", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<RoleDto> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (e.getMessage().contains("already exists")) {
                ApiResponse<RoleDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi cập nhật vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<RoleDto> response = ApiResponse.error("Lỗi khi cập nhật vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "🗑️ Xóa vai trò", 
        description = "Xóa một vai trò khỏi hệ thống"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "✅ Xóa thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", 
                description = "❌ Không tìm thấy vai trò"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "ID của vai trò", required = true, example = "1")
            @PathVariable Integer id) {
        try {
            roleService.deleteRole(id);
            ApiResponse<Void> response = ApiResponse.success("Xóa vai trò thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<Void> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa vai trò: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
