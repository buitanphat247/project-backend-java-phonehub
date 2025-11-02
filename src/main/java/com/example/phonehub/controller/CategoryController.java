package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.auth.annotation.RequiresAuth;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateCategoryRequest;
import com.example.phonehub.dto.CategoryDto;
import com.example.phonehub.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "API quản lý danh mục sản phẩm")
@Public
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "📄 Lấy danh sách danh mục có phân trang", description = "Trả về danh sách danh mục với phân trang")
    @Public
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getAllCategories(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng danh mục mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CategoryDto> categories = categoryService.getAllCategories(page, size);
            ApiResponse<Page<CategoryDto>> response = ApiResponse.success("Lấy danh sách danh mục thành công",
                    categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<CategoryDto>> response = ApiResponse
                    .error("Lỗi khi lấy danh sách danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🔍 Lấy danh mục theo ID")
    @Public
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer id) {
        try {
            Optional<CategoryDto> category = categoryService.getCategoryById(id);
            if (category.isPresent()) {
                ApiResponse<CategoryDto> response = ApiResponse.success("Lấy danh mục thành công", category.get());
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<CategoryDto> response = ApiResponse.notFound("Không tìm thấy danh mục với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            ApiResponse<CategoryDto> response = ApiResponse.error("Lỗi khi lấy danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "➕ Tạo danh mục mới")
    @RequiresAuth(roles = {"admin"})
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        try {
            CategoryDto category = categoryService.createCategory(request);
            ApiResponse<CategoryDto> response = ApiResponse.success("Tạo danh mục thành công", category);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                ApiResponse<CategoryDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<CategoryDto> response = ApiResponse.error("Lỗi khi tạo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<CategoryDto> response = ApiResponse.error("Lỗi khi tạo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "✏️ Cập nhật danh mục")
    @RequiresAuth(roles = {"admin"})
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer id,
            @Valid @RequestBody CreateCategoryRequest request) {
        try {
            CategoryDto category = categoryService.updateCategory(id, request);
            ApiResponse<CategoryDto> response = ApiResponse.success("Cập nhật danh mục thành công", category);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<CategoryDto> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (e.getMessage().contains("already exists")) {
                ApiResponse<CategoryDto> response = ApiResponse.conflict(e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            ApiResponse<CategoryDto> response = ApiResponse.error("Lỗi khi cập nhật danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<CategoryDto> response = ApiResponse.error("Lỗi khi cập nhật danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "🗑️ Xóa danh mục")
    @RequiresAuth(roles = {"admin"})
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            ApiResponse<Void> response = ApiResponse.success("Xóa danh mục thành công", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                ApiResponse<Void> response = ApiResponse.notFound(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.error("Lỗi khi xóa danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
