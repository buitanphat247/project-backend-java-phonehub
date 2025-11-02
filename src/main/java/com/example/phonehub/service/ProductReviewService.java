package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductReviewRequest;
import com.example.phonehub.dto.ProductReviewDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductReview;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.ProductReviewRepository;
import com.example.phonehub.repository.UserRepository;
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
    
    // Lấy tất cả reviews theo product ID (có phân trang)
    public Page<ProductReviewDto> getByProductId(Integer productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        return ProductUtils.toReviewDtoPage(reviewRepository.findByProductId(productId, PageRequest.of(page, size)));
    }
    
    // Lấy tất cả reviews theo user ID (có phân trang)
    public Page<ProductReviewDto> getByUserId(Integer userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return ProductUtils.toReviewDtoPage(reviewRepository.findByUserId(userId, PageRequest.of(page, size)));
    }
    
    // Lấy review theo ID
    public Optional<ProductReviewDto> getById(Integer id) {
        return reviewRepository.findById(id).map(ProductUtils::toDto);
    }
    
    // Kiểm tra user đã review sản phẩm này chưa
    public boolean hasUserReviewedProduct(Integer productId, Integer userId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }
    
    // Tạo review mới
    public ProductReviewDto create(CreateProductReviewRequest req) {
        // Kiểm tra product tồn tại
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Kiểm tra user tồn tại
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra user đã review sản phẩm này chưa (có thể cho phép nhiều review hoặc chỉ 1)
        Optional<ProductReview> existingReview = reviewRepository.findByProductIdAndUserId(req.getProductId(), req.getUserId());
        if (existingReview.isPresent()) {
            throw new RuntimeException("User has already reviewed this product. Please update your existing review instead.");
        }
        
        // Tạo review mới
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        
        return ProductUtils.toDto(reviewRepository.save(review));
    }
    
    // Cập nhật review
    public ProductReviewDto update(Integer id, CreateProductReviewRequest req) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Kiểm tra quyền (chỉ user tạo review mới được sửa)
        if (!review.getUser().getId().equals(req.getUserId())) {
            throw new RuntimeException("You can only update your own review");
        }
        
        // Cập nhật thông tin
        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());
        
        return ProductUtils.toDto(reviewRepository.save(review));
    }
    
    // Xóa review
    public void delete(Integer id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found");
        }
        reviewRepository.deleteById(id);
    }
    
    // Lấy điểm đánh giá trung bình của sản phẩm
    public Double getAverageRating(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    // Lấy số lượng review của sản phẩm
    public Long getReviewCount(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        return reviewRepository.countByProductId(productId);
    }
    
    // Lấy thống kê rating (phân bố điểm số)
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

