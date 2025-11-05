package com.example.phonehub.service;

import com.example.phonehub.dto.CreateOrderItemRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.entity.Order;
import com.example.phonehub.entity.OrderItem;
import com.example.phonehub.entity.Product;
import com.example.phonehub.repository.OrderItemRepository;
import com.example.phonehub.repository.OrderRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    // OrderHelper không còn cần thiết khi unitPrice được truyền từ client

    @Autowired
    private com.example.phonehub.service.redis_cache.OrderItemCacheService orderItemCacheService;
    @Autowired
    private com.example.phonehub.service.redis_cache.OrderCacheService orderCacheService;

    public Page<OrderItemDto> listByOrder(Integer orderId, int page, int size) {
        return orderItemCacheService.listByOrder(orderId, page, size);
    }

    public Optional<OrderItemDto> getById(Integer id) {
        return orderItemCacheService.byId(id);
    }

    public BigDecimal totalSpentByUser(Integer userId) {
        return orderCacheService.totalSpentByUser(userId);
    }

    public OrderDto addItem(Integer orderId, CreateOrderItemRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));

        // Kiểm tra và trừ tồn kho
        int remain = product.getQuantity() != null ? product.getQuantity() : 0;
        if (req.getQuantity() > remain) {
            throw new RuntimeException("Insufficient stock for product " + product.getId() + ": remain=" + remain);
        }
        product.setQuantity(remain - req.getQuantity());
        productRepository.save(product);

        BigDecimal unitPrice = req.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
        // Giữ unitPrice theo thời điểm mua; không dùng subtotal để cập nhật tổng

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(unitPrice);

        orderItemRepository.save(item);

        // Không cập nhật totalPrice tại đây; tổng tiền lấy theo amount khi tạo order
        Order saved = orderRepository.save(order);
        orderItemCacheService.evictAll();
        orderCacheService.evictAll();
        return OrderUtils.toDto(saved);
    }
}
