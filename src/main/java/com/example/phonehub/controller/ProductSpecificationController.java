package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductSpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-specifications")
@Tag(name = "Product Specification Management")
public class ProductSpecificationController {
    @Autowired private ProductSpecificationService specService;

    @Operation(summary = "📋 Lấy danh sách thông số kỹ thuật theo Product ID", description = "Lấy tất cả thông số kỹ thuật của sản phẩm. Có thể lọc theo group (tùy chọn)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSpecificationDto>>> byProduct(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @RequestParam Integer productId,
            @Parameter(description = "Tên nhóm thông số (tùy chọn)", example = "Màn hình") @RequestParam(required = false) String group){
        try {
            List<ProductSpecificationDto> data = group==null ? specService.getByProduct(productId) : specService.getByProductAndGroup(productId, group);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông số thành công", data));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "➕ Tạo thông số kỹ thuật mới", description = "Tạo một thông số kỹ thuật mới cho sản phẩm")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductSpecificationDto>> create(@Valid @RequestBody CreateProductSpecificationRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo thông số thành công", specService.create(req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @Operation(summary = "✏️ Cập nhật thông số kỹ thuật", description = "Cập nhật thông tin thông số kỹ thuật theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSpecificationDto>> update(
            @Parameter(description = "ID của thông số kỹ thuật", required = true, example = "1") @PathVariable Integer id, 
            @Valid @RequestBody CreateProductSpecificationRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật thông số thành công", specService.update(id, req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @Operation(summary = "🗑️ Xóa thông số kỹ thuật", description = "Xóa thông số kỹ thuật theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của thông số kỹ thuật", required = true, example = "1") @PathVariable Integer id){
        try { specService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa thông số thành công", null)); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



