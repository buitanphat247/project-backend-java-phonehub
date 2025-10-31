package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductSpecificationService;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSpecificationDto>>> byProduct(@RequestParam Integer productId, @RequestParam(required = false) String group){
        try {
            List<ProductSpecificationDto> data = group==null ? specService.getByProduct(productId) : specService.getByProductAndGroup(productId, group);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSpecificationDto>> create(@Valid @RequestBody CreateProductSpecificationRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo thông số thành công", specService.create(req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSpecificationDto>> update(@PathVariable Integer id, @Valid @RequestBody CreateProductSpecificationRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật thông số thành công", specService.update(id, req))); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id){
        try { specService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa thông số thành công", null)); }
        catch (RuntimeException e){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage())); }
    }
}



