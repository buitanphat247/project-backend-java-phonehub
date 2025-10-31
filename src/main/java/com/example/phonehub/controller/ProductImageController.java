package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-images")
@Tag(name = "Product Image Management")
public class ProductImageController {
    @Autowired private ProductImageService imageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> byProduct(@RequestParam Integer productId){
        try { return ResponseEntity.ok(ApiResponse.success(imageService.getByProduct(productId))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductImageDto>> create(@Valid @RequestBody CreateProductImageRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo ảnh thành công", imageService.create(req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductImageDto>> update(@PathVariable Integer id, @Valid @RequestBody CreateProductImageRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh thành công", imageService.update(id, req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id){
        try { imageService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa ảnh thành công", null)); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



