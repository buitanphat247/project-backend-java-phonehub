package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductColorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/product-colors")
@Tag(name = "Product Color Management")
public class ProductColorController {
    @Autowired private ProductColorService colorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductColorDto>>> byProduct(@RequestParam Integer productId){
        try { return ResponseEntity.ok(ApiResponse.success(colorService.getByProduct(productId))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductColorDto>> bySlug(@RequestParam String slug){
        try {
            Optional<ProductColorDto> c = colorService.getBySlug(slug);
            return c.map(v -> ResponseEntity.ok(ApiResponse.success(v)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy màu")));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductColorDto>> create(@Valid @RequestBody CreateProductColorRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo màu thành công", colorService.create(req))); }
        catch (RuntimeException e){ if (e.getMessage().contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(e.getMessage())); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductColorDto>> update(@PathVariable Integer id, @Valid @RequestBody CreateProductColorRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật màu thành công", colorService.update(id, req))); }
        catch (RuntimeException e){ String m=e.getMessage(); if (m.contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(m)); if (m.contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(m)); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(m)); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id){
        try { colorService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa màu thành công", null)); }
        catch (RuntimeException e){ if (e.getMessage().contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage())); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



