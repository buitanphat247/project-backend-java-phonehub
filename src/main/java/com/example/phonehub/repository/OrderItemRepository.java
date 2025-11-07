package com.example.phonehub.repository;

import com.example.phonehub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE oi.id = :orderItemId AND o.user IS NOT NULL AND o.user.id = :userId")
    Optional<OrderItem> findByIdAndUserId(@Param("orderItemId") Integer orderItemId, @Param("userId") Integer userId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o WHERE o.user IS NOT NULL AND o.user.id = :userId AND oi.product.id = :productId")
    Long countByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
}


