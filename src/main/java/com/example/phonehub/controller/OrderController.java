package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.CreateOrderRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@Public
@Tag(name = "Orders", description = "🧾 API đặt hàng (simulation mode)")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Danh sách orders", description = "Lấy danh sách orders với phân trang")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer userId) {
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

    @Operation(summary = "Chi tiết order", description = "Lấy chi tiết order theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getById(@PathVariable Integer id) {
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

    @Operation(summary = "Tạo order", description = "Tạo mới một order (simulation)")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dữ liệu tạo order",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "userId": 1,
                              "paymentMethod": "COD",
                              "note": "Giao giờ hành chính",
                              "items": [
                                { "productId": 1, "quantity": 2 },
                                { "productId": 3, "quantity": 1 }
                              ]
                            }
                            """))
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
}


