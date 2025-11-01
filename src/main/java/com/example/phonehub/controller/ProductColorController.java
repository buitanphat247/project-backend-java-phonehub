package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductColorService;
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
@RequestMapping("/api/v1/product-colors")
@Tag(name = "Product Color Management", description = "API quản lý màu sắc sản phẩm")
public class ProductColorController {
    @Autowired private ProductColorService colorService;

    @Operation(summary = "🎨 Lấy danh sách màu sắc theo Product ID", description = "Trả về danh sách tất cả màu sắc của sản phẩm")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductColorDto>>> byProduct(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @RequestParam Integer productId){
        try { return ResponseEntity.ok(ApiResponse.success(colorService.getByProduct(productId))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "➕ Tạo màu sắc mới", description = "Tạo một màu sắc mới cho sản phẩm")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductColorDto>> create(@Valid @RequestBody CreateProductColorRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo màu thành công", colorService.create(req))); }
        catch (RuntimeException e){ if (e.getMessage().contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(e.getMessage())); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @Operation(summary = "✏️ Cập nhật màu sắc", description = "Cập nhật thông tin màu sắc theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductColorDto>> update(
            @Parameter(description = "ID của màu sắc", required = true, example = "1") @PathVariable Integer id, 
            @Valid @RequestBody CreateProductColorRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật màu thành công", colorService.update(id, req))); }
        catch (RuntimeException e){ String m=e.getMessage(); if (m.contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(m)); if (m.contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(m)); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(m)); }
    }

    @Operation(summary = "🗑️ Xóa màu sắc", description = "Xóa màu sắc theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của màu sắc", required = true, example = "1") @PathVariable Integer id){
        try { colorService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa màu thành công", null)); }
        catch (RuntimeException e){ if (e.getMessage().contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage())); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



