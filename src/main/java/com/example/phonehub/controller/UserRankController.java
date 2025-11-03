package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.auth.annotation.RequiresAuth;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateUserRankRequest;
import com.example.phonehub.dto.UpdateUserRankRequest;
import com.example.phonehub.dto.UserRankDto;
import com.example.phonehub.service.UserRankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user-ranks")
@Tag(name = "User Rank Management", description = "API quản lý xếp hạng người dùng")
public class UserRankController {
    
    @Autowired
    private UserRankService rankService;
    
    @Operation(summary = "📄 Lấy danh sách tất cả các rank", description = "Lấy danh sách tất cả các rank levels (sắp xếp theo điểm từ thấp đến cao)")
    @GetMapping
    @Public
    public ResponseEntity<ApiResponse<List<UserRankDto>>> getAllRanks() {
        try {
            List<UserRankDto> ranks = rankService.getAllRanks();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách rank thành công", ranks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🔍 Lấy rank theo ID", description = "Lấy thông tin chi tiết một rank theo ID")
    @GetMapping("/{id}")
    @Public
    public ResponseEntity<ApiResponse<UserRankDto>> getById(
            @Parameter(description = "ID của rank", required = true, example = "1") @PathVariable Integer id) {
        try {
            Optional<UserRankDto> rank = rankService.getById(id);
            return rank.map(r -> ResponseEntity.ok(ApiResponse.success("Lấy rank thành công", r)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy rank")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🔍 Lấy rank theo tên", description = "Lấy thông tin rank theo tên")
    @GetMapping("/name/{name}")
    @Public
    public ResponseEntity<ApiResponse<UserRankDto>> getByName(
            @Parameter(description = "Tên của rank", required = true, example = "Vàng") @PathVariable String name) {
        try {
            Optional<UserRankDto> rank = rankService.getByName(name);
            return rank.map(r -> ResponseEntity.ok(ApiResponse.success("Lấy rank thành công", r)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy rank")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🔍 Lấy rank dựa trên điểm số", description = "Xác định rank phù hợp dựa trên điểm số")
    @GetMapping("/by-points/{points}")
    @Public
    public ResponseEntity<ApiResponse<UserRankDto>> getByPoints(
            @Parameter(description = "Điểm số", required = true, example = "500") @PathVariable Integer points) {
        try {
            Optional<UserRankDto> rank = rankService.getRankByPoints(points);
            return rank.map(r -> ResponseEntity.ok(ApiResponse.success("Lấy rank thành công", r)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy rank phù hợp")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "➕ Tạo rank mới", description = "Tạo một rank level mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Tạo rank thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "❌ Rank đã tồn tại hoặc khoảng điểm bị overlap")
    })
    @PostMapping
    @RequiresAuth(roles = {"admin"})
    public ResponseEntity<ApiResponse<UserRankDto>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin rank mới",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateUserRankRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "Vàng",
                          "minPoints": 500,
                          "maxPoints": 999,
                          "discount": 5.00
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateUserRankRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo rank thành công", rankService.create(req)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("already exists") || msg.contains("overlaps")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "✏️ Cập nhật rank", description = "Cập nhật thông tin rank (các field là optional, chỉ gửi field cần cập nhật)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật rank thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy rank"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ")
    })
    @PutMapping("/{id}")
    @RequiresAuth(roles = {"admin"})
    public ResponseEntity<ApiResponse<UserRankDto>> update(
            @Parameter(description = "ID của rank", required = true, example = "1") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cập nhật rank (tất cả fields đều optional, chỉ gửi field cần cập nhật)",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateUserRankRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "Kim Cương",
                          "minPoints": 5000
                        }
                        """)
                )
            )
            @Valid @RequestBody UpdateUserRankRequest req) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Cập nhật rank thành công", rankService.update(id, req)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            }
            if (msg.contains("already exists") || msg.contains("overlaps")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🗑️ Xóa rank", description = "Xóa một rank theo ID")
    @DeleteMapping("/{id}")
    @RequiresAuth(roles = {"admin"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của rank", required = true, example = "1") @PathVariable Integer id) {
        try {
            rankService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa rank thành công", null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}

