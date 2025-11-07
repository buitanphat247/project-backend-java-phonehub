package com.example.phonehub.repository;

import com.example.phonehub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE oi.id = :orderItemId AND o.user IS NOT NULL AND o.user.id = :userId")
    Optional<OrderItem> findByIdAndUserId(@Param("orderItemId") Integer orderItemId, @Param("userId") Integer userId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o WHERE o.user IS NOT NULL AND o.user.id = :userId AND oi.product.id = :productId")
    Long countByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    Page<OrderItem> findByOrder_Id(Integer orderId, Pageable pageable);

    Optional<OrderItem> findByOrder_IdAndProduct_Id(Integer orderId, Integer productId);

    Optional<OrderItem> findByReview_Id(Integer reviewId);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.isReviewed = :isReviewed, oi.review.id = :reviewId WHERE oi.id = :orderItemId")
    void updateReviewState(@Param("orderItemId") Integer orderItemId,
                           @Param("isReviewed") Boolean isReviewed,
                           @Param("reviewId") Integer reviewId);
}


