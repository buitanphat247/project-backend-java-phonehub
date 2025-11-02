package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
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
    @Public
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", productService.getAll(page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm đã xuất bản", description = "Trả về danh sách sản phẩm đã được xuất bản với phân trang")
    @GetMapping("/published")
    @Public
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getPublished(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success(productService.getPublished(page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm theo danh mục", description = "Trả về danh sách sản phẩm đã xuất bản theo danh mục với phân trang")
    @GetMapping("/published/category/{categoryId}")
    @Public
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getByCategory(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer categoryId, 
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size){
        try { return ResponseEntity.ok(ApiResponse.success(productService.getPublishedByCategory(categoryId,page,size))); }
        catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "📄 Lấy danh sách sản phẩm theo thương hiệu", description = "Trả về danh sách sản phẩm đã xuất bản theo thương hiệu (và danh mục nếu có) với phân trang")
    @GetMapping("/published/brand")
    @Public
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getByBrand(
            @Parameter(description = "Tên thương hiệu", required = true, example = "Samsung") @RequestParam String brand,
            @Parameter(description = "ID của danh mục (optional, để lọc chính xác hơn)", example = "1") @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size){
        try {
            Page<ProductDto> products;
            if (categoryId != null) {
                products = productService.getPublishedByBrandAndCategory(brand, categoryId, page, size);
            } else {
                products = productService.getPublishedByBrand(brand, page, size);
            }
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🔍 Lấy sản phẩm theo ID", description = "Trả về thông tin chi tiết sản phẩm theo ID")
    @GetMapping("/{id}")
    @Public
    public ResponseEntity<ApiResponse<ProductDto>> getById(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer id){
        try {
            Optional<ProductDto> p = productService.getById(id);
            return p.map(productDto -> ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm thành công", productDto)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy sản phẩm")));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🔎 Tìm sản phẩm theo tên", description = "Tìm kiếm sản phẩm theo tên (tìm kiếm mờ - partial match) với phân trang. Có thể kèm theo categoryId để lọc chính xác hơn")
    @Public
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> searchByName(
            @Parameter(description = "Tên sản phẩm cần tìm", required = true, example = "OPPO Pad") @RequestParam String name,
            @Parameter(description = "ID của danh mục (optional, để lọc chính xác hơn)", example = "1") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng sản phẩm mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size){
        try {
            Page<ProductDto> products;
            if (categoryId != null) {
                products = productService.searchByNameAndCategory(name, categoryId, page, size);
            } else {
                products = productService.searchByName(name, page, size);
            }
            return ResponseEntity.ok(ApiResponse.success("Tìm thấy " + products.getTotalElements() + " sản phẩm", products));
        } catch (Exception e){ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage())); }
    }

    @Operation(summary = "🏷️ Lấy danh sách thương hiệu theo danh mục", description = "Trả về danh sách tất cả thương hiệu (brand) của sản phẩm đã xuất bản trong một danh mục cụ thể")
    @GetMapping("/brands/category/{categoryId}")
    @Public
    public ResponseEntity<ApiResponse<java.util.List<String>>> getBrandsByCategory(
            @Parameter(description = "ID của danh mục", required = true, example = "1") @PathVariable Integer categoryId){
        try {
            java.util.List<String> brands = productService.getBrandsByCategory(categoryId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thương hiệu thành công (" + brands.size() + " thương hiệu)", brands));
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



