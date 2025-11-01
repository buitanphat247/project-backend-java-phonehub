package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductImageService;
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
@RequestMapping("/api/v1/product-images")
@Tag(name = "Product Image Management", description = "API quản lý hình ảnh sản phẩm")
public class ProductImageController {
    @Autowired private ProductImageService imageService;

    @Operation(summary = "🖼️ Lấy danh sách hình ảnh theo Product ID", description = "Trả về danh sách tất cả hình ảnh của sản phẩm")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> byProduct(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @RequestParam Integer productId){
        try { return ResponseEntity.ok(ApiResponse.success(imageService.getByProduct(productId))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "➕ Tạo hình ảnh mới", description = "Tạo một hình ảnh mới cho sản phẩm")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductImageDto>> create(@Valid @RequestBody CreateProductImageRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo ảnh thành công", imageService.create(req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @Operation(summary = "✏️ Cập nhật hình ảnh", description = "Cập nhật thông tin hình ảnh theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductImageDto>> update(
            @Parameter(description = "ID của hình ảnh", required = true, example = "1") @PathVariable Integer id, 
            @Valid @RequestBody CreateProductImageRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh thành công", imageService.update(id, req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @Operation(summary = "🗑️ Xóa hình ảnh", description = "Xóa hình ảnh theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của hình ảnh", required = true, example = "1") @PathVariable Integer id){
        try { imageService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa ảnh thành công", null)); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



