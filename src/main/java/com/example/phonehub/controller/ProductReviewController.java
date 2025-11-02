package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.auth.annotation.RequiresAuth;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateProductReviewRequest;
import com.example.phonehub.dto.ProductReviewDto;
import com.example.phonehub.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/product-reviews")
@Tag(name = "Product Reviews Management", description = "API quản lý đánh giá sản phẩm")
public class ProductReviewController {
    
    @Autowired
    private ProductReviewService reviewService;
    
    @Operation(summary = "📄 Lấy danh sách đánh giá theo Product ID", description = "Lấy tất cả đánh giá của sản phẩm với phân trang")
    @GetMapping("/product/{productId}")
    @Public
    public ResponseEntity<ApiResponse<Page<ProductReviewDto>>> getByProductId(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng đánh giá mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", reviewService.getByProductId(productId, page, size)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "📄 Lấy danh sách đánh giá theo User ID", description = "Lấy tất cả đánh giá của người dùng với phân trang")
    @GetMapping("/user/{userId}")
    @RequiresAuth
    public ResponseEntity<ApiResponse<Page<ProductReviewDto>>> getByUserId(
            @Parameter(description = "ID của người dùng", required = true, example = "1") @PathVariable Integer userId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng đánh giá mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", reviewService.getByUserId(userId, page, size)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🔍 Lấy đánh giá theo ID", description = "Lấy thông tin chi tiết một đánh giá theo ID")
    @GetMapping("/{id}")
    @Public
    public ResponseEntity<ApiResponse<ProductReviewDto>> getById(
            @Parameter(description = "ID của đánh giá", required = true, example = "1") @PathVariable Integer id) {
        try {
            Optional<ProductReviewDto> review = reviewService.getById(id);
            return review.map(r -> ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công", r)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Không tìm thấy đánh giá")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "⭐ Lấy điểm đánh giá trung bình", description = "Lấy điểm đánh giá trung bình (1-5 sao) của sản phẩm")
    @GetMapping("/product/{productId}/average-rating")
    @Public
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId) {
        try {
            Double avgRating = reviewService.getAverageRating(productId);
            return ResponseEntity.ok(ApiResponse.success("Lấy điểm đánh giá trung bình thành công", avgRating));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "📊 Lấy số lượng đánh giá", description = "Lấy tổng số lượng đánh giá của sản phẩm")
    @GetMapping("/product/{productId}/count")
    @Public
    public ResponseEntity<ApiResponse<Long>> getReviewCount(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId) {
        try {
            Long count = reviewService.getReviewCount(productId);
            return ResponseEntity.ok(ApiResponse.success("Lấy số lượng đánh giá thành công", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "📈 Lấy phân bố điểm đánh giá", description = "Lấy thống kê phân bố điểm số (1-5 sao) của sản phẩm")
    @GetMapping("/product/{productId}/rating-distribution")
    @Public
    public ResponseEntity<ApiResponse<Map<Integer, Long>>> getRatingDistribution(
            @Parameter(description = "ID của sản phẩm", required = true, example = "1") @PathVariable Integer productId) {
        try {
            Map<Integer, Long> distribution = reviewService.getRatingDistribution(productId);
            return ResponseEntity.ok(ApiResponse.success("Lấy phân bố điểm đánh giá thành công", distribution));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "➕ Tạo đánh giá mới", description = "Tạo một đánh giá mới cho sản phẩm (mỗi user chỉ được đánh giá 1 lần cho mỗi sản phẩm)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Tạo đánh giá thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "❌ User đã đánh giá sản phẩm này rồi")
    })
    @PostMapping
    @RequiresAuth
    public ResponseEntity<ApiResponse<ProductReviewDto>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đánh giá mới",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateProductReviewRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "productId": 1,
                          "userId": 1,
                          "rating": 5,
                          "comment": "Tôi rất hài lòng với sản phẩm này. Chất lượng tốt, giá cả hợp lý."
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateProductReviewRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo đánh giá thành công", reviewService.create(req)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("already reviewed")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.conflict(msg));
            }
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "✏️ Cập nhật đánh giá", description = "Cập nhật thông tin đánh giá (chỉ user tạo review mới được sửa)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật đánh giá thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy đánh giá"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "❌ Không có quyền sửa đánh giá này")
    })
    @PutMapping("/{id}")
    @RequiresAuth
    public ResponseEntity<ApiResponse<ProductReviewDto>> update(
            @Parameter(description = "ID của đánh giá", required = true, example = "1") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cập nhật đánh giá",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateProductReviewRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "productId": 1,
                          "userId": 1,
                          "rating": 4,
                          "comment": "Sản phẩm khá tốt nhưng còn một số điểm cần cải thiện."
                        }
                        """)
                )
            )
            @Valid @RequestBody CreateProductReviewRequest req) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công", reviewService.update(id, req)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(msg));
            }
            if (msg.contains("only update your own")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "🗑️ Xóa đánh giá", description = "Xóa một đánh giá theo ID")
    @DeleteMapping("/{id}")
    @RequiresAuth(roles = {"admin"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID của đánh giá", required = true, example = "1") @PathVariable Integer id) {
        try {
            reviewService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}

