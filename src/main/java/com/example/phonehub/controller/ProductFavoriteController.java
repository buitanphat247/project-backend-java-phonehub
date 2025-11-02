package com.example.phonehub.controller;

import com.example.phonehub.auth.util.JwtUtil;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateProductFavoriteRequest;
import com.example.phonehub.dto.ProductFavoriteDto;
import com.example.phonehub.service.ProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Product Favorites", description = "API quản lý sản phẩm yêu thích")
public class ProductFavoriteController {
    @Autowired private ProductFavoriteService favoriteService;
    @Autowired private JwtUtil jwtUtil;

    private Integer getCurrentUserId(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) 
            throw new RuntimeException("Unauthorized: Token is required");
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    @Operation(summary = "❤️ Thêm sản phẩm vào yêu thích", description = "Thêm sản phẩm vào danh sách yêu thích của người dùng hiện tại")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductFavoriteDto>> addFavorite(
            @Valid @RequestBody CreateProductFavoriteRequest req, HttpServletRequest request){
        try {
            Integer userId = getCurrentUserId(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm vào yêu thích", favoriteService.addFavorite(userId, req)));
        } catch (RuntimeException e){
            String msg = e.getMessage();
            if (msg.contains("đã có trong danh sách")) return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(msg));
            if (msg.contains("not found")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            if (msg.contains("Unauthorized")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(msg));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage()));
        }
    }

    @Operation(summary = "❌ Xóa sản phẩm khỏi yêu thích", description = "Xóa sản phẩm khỏi danh sách yêu thích của người dùng hiện tại")
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId,
            HttpServletRequest request){
        try {
            Integer userId = getCurrentUserId(request);
            favoriteService.removeFavorite(userId, productId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa khỏi yêu thích", null));
        } catch (RuntimeException e){
            String msg = e.getMessage();
            if (msg.contains("không có trong danh sách")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            if (msg.contains("Unauthorized")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(msg));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage()));
        }
    }

    @Operation(summary = "📄 Lấy danh sách yêu thích", description = "Lấy danh sách sản phẩm yêu thích của người dùng hiện tại với phân trang")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductFavoriteDto>>> getUserFavorites(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng sản phẩm mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request){
        try {
            Integer userId = getCurrentUserId(request);
            Page<ProductFavoriteDto> favorites = favoriteService.getUserFavorites(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu thích thành công (" + favorites.getTotalElements() + " sản phẩm)", favorites));
        } catch (RuntimeException e){
            if (e.getMessage().contains("Unauthorized")) 
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage()));
        }
    }

    @Operation(summary = "✅ Kiểm tra sản phẩm có trong yêu thích", description = "Kiểm tra xem sản phẩm có trong danh sách yêu thích của người dùng hiện tại không")
    @GetMapping("/product/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId,
            HttpServletRequest request){
        try {
            Integer userId = getCurrentUserId(request);
            boolean isFavorite = favoriteService.isFavorite(userId, productId);
            return ResponseEntity.ok(ApiResponse.success(isFavorite ? "Sản phẩm đã có trong yêu thích" : "Sản phẩm chưa có trong yêu thích", isFavorite));
        } catch (RuntimeException e){
            if (e.getMessage().contains("Unauthorized")) 
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage()));
        }
    }

    @Operation(summary = "📊 Đếm số lượng yêu thích", description = "Đếm tổng số lượng người dùng đã yêu thích sản phẩm")
    @GetMapping("/product/{productId}/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId){
        try {
            long count = favoriteService.getFavoriteCount(productId);
            return ResponseEntity.ok(ApiResponse.success("Số lượng yêu thích: " + count, count));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: "+e.getMessage()));
        }
    }
}

