package com.example.phonehub.service;

import com.example.phonehub.dto.CreateOrderItemRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.entity.Order;
import com.example.phonehub.entity.OrderItem;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductReview;
import com.example.phonehub.repository.OrderItemRepository;
import com.example.phonehub.repository.OrderRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.ProductReviewRepository;
import com.example.phonehub.utils.OrderUtils;
import jakarta.transaction.Transactional;
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
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private com.example.phonehub.service.redis_cache.OrderItemCacheService orderItemCacheService;
    @Autowired
    private com.example.phonehub.service.redis_cache.OrderCacheService orderCacheService;

    /**
     * Cập nhật trạng thái review cho order item.
     */
    @Transactional
    public OrderItemDto updateReviewState(Integer orderItemId, boolean reviewed, Integer reviewId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + orderItemId));

        if (reviewed) {
            if (reviewId == null) {
                throw new RuntimeException("Review ID is required when marking item as reviewed");
            }
            ProductReview review = productReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
            if (!review.getOrder().getId().equals(item.getOrder().getId()) ||
                    !review.getProduct().getId().equals(item.getProduct().getId())) {
                throw new RuntimeException("Review does not belong to the same order item");
            }
            item.setReview(review);
            item.setIsReviewed(true);
        } else {
            item.setReview(null);
            item.setIsReviewed(false);
        }

        OrderItem saved = orderItemRepository.save(item);
        orderItemCacheService.evictAll();
        orderCacheService.evictAll();
        return OrderUtils.toItemDto(saved);
    }

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

    /**
     * Kiểm tra xem user có mua order item này không
     * @param userId ID của user
     * @param orderItemId ID của order item
     * @return true nếu user đã mua order item này, false nếu không
     */
    public boolean checkUserPurchasedItem(Integer userId, Integer orderItemId) {
        if (userId == null || orderItemId == null) {
            return false;
        }
        return orderItemRepository.findByIdAndUserId(orderItemId, userId).isPresent();
    }

}
