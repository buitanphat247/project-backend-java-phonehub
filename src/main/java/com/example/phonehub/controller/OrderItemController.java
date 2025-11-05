package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.dto.CreateOrderItemRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order Items", description = "📦 API Order Items: quản lý items độc lập. Order có thể tạo trước không có items; thêm/sửa items qua các endpoint riêng.")
@Public
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @Operation(summary = "Danh sách items theo order", description = "Phân trang danh sách items thuộc một order. Trả về các trường: id, productId, productName, quantity, unitPrice, createdAt.")
    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<ApiResponse<Page<OrderItemDto>>> listByOrder(
            @PathVariable Integer orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<OrderItemDto> data = orderItemService.listByOrder(orderId, page, size);
            return ResponseEntity.ok(ApiResponse.success("Success", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy items: " + e.getMessage()));
        }
    }

    @Operation(summary = "Chi tiết item", description = "Lấy chi tiết một order item theo ID. Trả về các trường: id, productId, productName, quantity, unitPrice, createdAt.")
    @GetMapping("/order-items/{id}")
    public ResponseEntity<ApiResponse<OrderItemDto>> getById(@PathVariable Integer id) {
        try {
            Optional<OrderItemDto> item = orderItemService.getById(id);
            return item.map(i -> ResponseEntity.ok(ApiResponse.success("Success", i)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Order item not found", 404)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy chi tiết item: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Thêm item vào order",
            description = "Tạo một order item mới cho order hiện có. Truyền productId, quantity, unitPrice (VND tại thời điểm mua). Tổng tiền order = sum(unit_price * quantity).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                            {
                              "productId": 1,
                              "quantity": 2,
                              "unitPrice": 15990000.00
                            }
                            """)
                    )
            )
    )
    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<ApiResponse<OrderDto>> addItem(
            @PathVariable Integer orderId,
            @Valid @RequestBody CreateOrderItemRequest req) {
        try {
            OrderDto updated = orderItemService.addItem(orderId, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Thêm item thành công", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi thêm item: " + e.getMessage()));
        }
    }

    @Operation(summary = "Tổng chi tiêu của user", description = "Tính tổng số tiền đã chi tiêu dựa trên các order có status=success của userId")
    @GetMapping("/users/{userId}/order-items/total-spent")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> totalSpent(@PathVariable Integer userId) {
        try {
            java.math.BigDecimal total = orderItemService.totalSpentByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Success", total));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tính tổng chi tiêu: " + e.getMessage()));
        }
    }
}
