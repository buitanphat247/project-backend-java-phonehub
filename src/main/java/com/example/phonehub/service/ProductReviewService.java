package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductReviewRequest;
import com.example.phonehub.dto.ProductReviewDto;
import com.example.phonehub.entity.Order;
import com.example.phonehub.entity.OrderItem;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductReview;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.OrderItemRepository;
import com.example.phonehub.repository.OrderRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.ProductReviewRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.redis_cache.OrderCacheService;
import com.example.phonehub.service.redis_cache.OrderItemCacheService;
import com.example.phonehub.service.redis_cache.ProductReviewCacheService;
import com.example.phonehub.utils.ProductUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReviewService {
    
    @Autowired
    private ProductReviewRepository reviewRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductReviewCacheService reviewCacheService;
    
    @Autowired
    private OrderItemCacheService orderItemCacheService;
    
    @Autowired
    private OrderCacheService orderCacheService;
    
    public Page<ProductReviewDto> getByProductId(Integer productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        return ProductUtils.toReviewDtoPage(reviewRepository.findByProductId(productId, PageRequest.of(page, size)));
    }
    
    public Page<ProductReviewDto> getByUserId(Integer userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return ProductUtils.toReviewDtoPage(reviewRepository.findByUserId(userId, PageRequest.of(page, size)));
    }
    
    public Optional<ProductReviewDto> getById(Integer id) {
        return reviewRepository.findById(id).map(ProductUtils::toDto);
    }
    
    public boolean hasUserReviewedProduct(Integer productId, Integer userId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }
    
    public ProductReviewDto create(CreateProductReviewRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getUser() != null && !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Order does not belong to this user");
        }
        
        OrderItem orderItem = orderItemRepository.findByOrder_IdAndProduct_Id(order.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Order item not found for this order and product"));
        
        if (Boolean.TRUE.equals(orderItem.getIsReviewed())) {
            throw new RuntimeException("This order item has already been reviewed");
        }
        
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setOrder(order);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        
        ProductReview saved = reviewRepository.save(review);
        orderItem.setReview(saved);
        orderItem.setIsReviewed(true);
        orderItemRepository.save(orderItem);
        
        reviewCacheService.invalidateProductReviewCache(req.getProductId());
        reviewCacheService.invalidateUserReviewCache(req.getUserId());
        orderItemCacheService.evictAll();
        orderCacheService.evictAll();
        
        return ProductUtils.toDto(saved);
    }
    
    public ProductReviewDto update(Integer id, CreateProductReviewRequest req) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getUser().getId().equals(req.getUserId())) {
            throw new RuntimeException("You can only update your own review");
        }
        
        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());
        
        ProductReviewDto updatedDto = ProductUtils.toDto(reviewRepository.save(review));
        reviewCacheService.invalidateProductReviewCache(review.getProduct().getId());
        reviewCacheService.invalidateUserReviewCache(review.getUser().getId());
        orderItemCacheService.evictAll();
        orderCacheService.evictAll();
        return updatedDto;
    }

    public void delete(Integer id) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        Integer productId = review.getProduct().getId();
        Integer userId = review.getUser().getId();
        orderItemRepository.findByReview_Id(id).ifPresent(item -> {
            item.setIsReviewed(false);
            item.setReview(null);
            orderItemRepository.save(item);
        });
        reviewRepository.deleteById(id);
        reviewCacheService.invalidateProductReviewCache(productId);
        reviewCacheService.invalidateUserReviewCache(userId);
        orderItemCacheService.evictAll();
        orderCacheService.evictAll();
    }
    
    public Double getAverageRating(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    public Long getReviewCount(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        return reviewRepository.countByProductId(productId);
    }
    
    public Map<Integer, Long> getRatingDistribution(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);
        return distribution.stream()
                .collect(Collectors.toMap(
                        obj -> ((Number) obj[0]).intValue(),
                        obj -> ((Number) obj[1]).longValue()
                ));
    }
}

