package com.example.phonehub.controller;

import com.example.phonehub.dto.*;
import com.example.phonehub.service.ProductService;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "API quản lý sản phẩm")
public class ProductController {
    @Autowired private ProductService productService;

    @Operation(summary = "📄 Lấy danh sách sản phẩm có phân trang", description = "Trả về danh sách tất cả sản phẩm với phân trang")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", productService.getAll(page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm đã xuất bản", description = "Trả về danh sách sản phẩm đã được xuất bản với phân trang")
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getPublished(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success(productService.getPublished(page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm theo danh mục", description = "Trả về danh sách sản phẩm đã xuất bản theo danh mục với phân trang")
    @GetMapping("/published/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getByCategory(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer categoryId, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success(productService.getPublishedByCategory(categoryId,page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm theo thương hiệu", description = "Trả về danh sách sản phẩm đã xuất bản theo thương hiệu với phân trang")
    @GetMapping("/published/brand")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getByBrand(
            @Parameter(description = "Tên thương hiệu", required = true, example = "OPPO") @RequestParam String brand, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success(productService.getPublishedByBrand(brand,page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🔍 Lấy sản phẩm theo ID", description = "Trả về thông tin chi tiết sản phẩm theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getById(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer id){
        try {
            Optional<ProductDto> p = productService.getById(id);
            return p.map(productDto -> ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm thành công", productDto)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy sản phẩm")));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🔎 Tìm sản phẩm theo slug", description = "Tìm kiếm sản phẩm theo slug")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductDto>> getBySlug(
            @Parameter(description = "Slug của sản phẩm", required = true, example = "may-tinh-bang-oppo-pad-se") @RequestParam String slug){
        try {
            Optional<ProductDto> p = productService.getBySlug(slug);
            return p.map(productDto -> ResponseEntity.ok(ApiResponse.success("Tìm thấy sản phẩm", productDto)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy sản phẩm")));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "➕ Tạo sản phẩm mới", description = "Tạo một sản phẩm mới trong hệ thống")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> create(@Valid @RequestBody CreateProductRequest req){
        try { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo sản phẩm thành công", productService.create(req))); }
        catch (RuntimeException e){
            if (e.getMessage().contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "✏️ Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer id, 
            @Valid @RequestBody CreateProductRequest req){
        try { return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", productService.update(id, req))); }
        catch (RuntimeException e){
            String msg=e.getMessage();
            if (msg.contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            if (msg.contains("already exists")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(msg));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🗑️ Xóa sản phẩm", description = "Xóa sản phẩm theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer id){
        try { productService.delete(id); return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null)); }
        catch (RuntimeException e){
            if (e.getMessage().contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }
}



