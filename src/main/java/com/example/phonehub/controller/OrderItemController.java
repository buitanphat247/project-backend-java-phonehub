package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.dto.CreateOrderItemRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.service.OrderItemService;
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

    @Operation(
            summary = "📋 Danh sách items theo order",
            description = "Phân trang danh sách items thuộc một order. Trả về các trường: id, productId, productName, quantity, unitPrice, createdAt."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Order không tồn tại")
    })
    @GetMapping("/orders/{orderId}/items")
    public ResponseEntity<ApiResponse<Page<OrderItemDto>>> listByOrder(
            @Parameter(description = "ID của order", required = true, example = "1") @PathVariable Integer orderId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng items mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
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

    @Operation(
            summary = "🔍 Chi tiết order item",
            description = "Lấy chi tiết một order item theo ID. Trả về các trường: id, productId, productName, quantity, unitPrice, createdAt."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy chi tiết thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Order item không tồn tại")
    })
    @GetMapping("/order-items/{id}")
    public ResponseEntity<ApiResponse<OrderItemDto>> getById(
            @Parameter(description = "ID của order item", required = true, example = "1") @PathVariable Integer id) {
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
            summary = "➕ Thêm item vào order",
            description = "Tạo một order item mới cho order hiện có. Truyền productId, quantity, unitPrice (VND tại thời điểm mua). Tổng tiền order = sum(unit_price * quantity)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Thêm item thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ hoặc không đủ tồn kho"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Order hoặc Product không tồn tại")
    })
    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<ApiResponse<OrderDto>> addItem(
            @Parameter(description = "ID của order", required = true, example = "1") @PathVariable Integer orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin order item",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderItemRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "productId": 1,
                              "quantity": 2,
                              "unitPrice": 15990000.00
                            }
                            """)
                    )
            )
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

    @Operation(
            summary = "💰 Tổng chi tiêu của user",
            description = "Tính tổng số tiền đã chi tiêu dựa trên các order có status=success của userId"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy tổng chi tiêu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ User không tồn tại")
    })
    @GetMapping("/users/{userId}/order-items/total-spent")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> totalSpent(
            @Parameter(description = "ID của user", required = true, example = "1") @PathVariable Integer userId) {
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

    @Operation(
            summary = "✅ Kiểm tra user có mua order item",
            description = "Kiểm tra xem user có userId có mua order item có orderItemId không. Trả về true nếu đã mua, false nếu chưa mua."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Kiểm tra thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    @GetMapping("/users/{userId}/order-items/{orderItemId}/check-purchased")
    public ResponseEntity<ApiResponse<Boolean>> checkUserPurchasedItem(
            @Parameter(description = "ID của user", required = true, example = "1") @PathVariable Integer userId,
            @Parameter(description = "ID của order item", required = true, example = "1") @PathVariable Integer orderItemId) {
        try {
            boolean purchased = orderItemService.checkUserPurchasedItem(userId, orderItemId);
            String message = purchased 
                    ? "User đã mua order item này" 
                    : "User chưa mua order item này";
            return ResponseEntity.ok(ApiResponse.success(message, purchased));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra: " + e.getMessage()));
        }
    }

}
