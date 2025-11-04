package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CartItemDto;
import com.example.phonehub.dto.CreateCartItemRequest;
import com.example.phonehub.dto.UpdateCartItemRequest;
import com.example.phonehub.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Public
@Tag(name = "Cart", description = "🛒 API giỏ hàng")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "Lấy giỏ hàng người dùng")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCart(@RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Success", cartService.getUserCart(userId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }

    @Operation(summary = "Thêm/cập nhật sản phẩm vào giỏ (upsert)")
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDto>> add(@Valid @RequestBody CreateCartItemRequest req) {
        try {
            CartItemDto item = cartService.addOrUpdate(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Thêm vào giỏ thành công", item));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi thêm vào giỏ: " + e.getMessage()));
        }
    }

    @Operation(summary = "Cập nhật số lượng")
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDto>> update(
            @PathVariable Integer cartItemId,
            @Valid @RequestBody UpdateCartItemRequest req) {
        try {
            CartItemDto item = cartService.updateQuantity(cartItemId, req);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", item));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật giỏ hàng: " + e.getMessage()));
        }
    }

    @Operation(summary = "Xóa 1 dòng giỏ hàng")
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Boolean>> remove(@PathVariable Integer cartItemId) {
        try {
            cartService.removeItem(cartItemId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa giỏ hàng: " + e.getMessage()));
        }
    }

    @Operation(summary = "Xóa toàn bộ giỏ hàng của user")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Boolean>> clear(@RequestParam Integer userId) {
        try {
            cartService.clearUserCart(userId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa giỏ hàng", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa giỏ hàng: " + e.getMessage()));
        }
    }
}


