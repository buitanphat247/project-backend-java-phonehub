package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateOrderRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.dto.UpdateOrderStatusRequest;
import com.example.phonehub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@Public
@Tag(name = "Orders", description = "🧾 API đặt hàng. Trạng thái: pending/success/failed. Có thể tạo order trước (không cần items), rồi thêm items sau qua module Order Items.")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(
            summary = "📋 Danh sách orders",
            description = "Lấy danh sách orders với phân trang. Trạng thái có thể là pending/success/failed. Có thể filter theo userId (tùy chọn)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> list(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng orders mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID của user (tùy chọn, để filter orders theo user)", example = "1") @RequestParam(required = false) Integer userId) {
        try {
            Page<OrderDto> data = (userId == null)
                    ? orderService.getOrders(page, size)
                    : orderService.getOrdersByUser(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success("Success", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách orders: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "🔍 Chi tiết order",
            description = "Lấy chi tiết order theo ID. Bao gồm thông tin buyer, items, status, payment method."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy chi tiết thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Order không tồn tại")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getById(
            @Parameter(description = "ID của order", required = true, example = "1") @PathVariable Integer id) {
        try {
            Optional<OrderDto> order = orderService.getById(id);
            return order.map(o -> ResponseEntity.ok(ApiResponse.success("Success", o)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Order not found", 404)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy chi tiết order: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "➕ Tạo order",
            description = "Tạo mới một order không cần items. Truyền trực tiếp tổng tiền (amount). Mặc định status=\"pending\". Sau khi tạo, có thể thêm items qua endpoint POST /orders/{orderId}/items."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Tạo order thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ User không tồn tại")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dữ liệu tạo order",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "userId": 1,
                              "buyerName": "Nguyễn Văn A",
                              "buyerEmail": "nguyenvana@example.com",
                              "buyerPhone": "0912345678",
                              "buyerAddress": "123 Đường ABC, Quận XYZ, TP.HCM",
                              "paymentMethod": "VNPAY",
                              "amount": 37900000.00
                            }
                            """)
                    )
            )
            @Valid @RequestBody CreateOrderRequest req) {
        try {
            OrderDto created = orderService.createOrder(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo order thành công", created));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo order: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "✏️ Cập nhật trạng thái order",
            description = "Chỉ cập nhật field status: pending/success/failed; các trường khác giữ nguyên."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật trạng thái thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Status không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Order không tồn tại")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateStatus(
            @Parameter(description = "ID của order", required = true, example = "1") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Trạng thái mới của order",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateOrderStatusRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": "success"
                            }
                            """)
                    )
            )
            @Valid @RequestBody UpdateOrderStatusRequest req) {
        try {
            OrderDto updated = orderService.updateStatus(id, req.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật trạng thái: " + e.getMessage()));
        }
    }
}


