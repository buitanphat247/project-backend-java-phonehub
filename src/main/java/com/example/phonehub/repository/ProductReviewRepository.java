package com.example.phonehub.repository;

import com.example.phonehub.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {
    
    @EntityGraph(attributePaths = {"product", "user"})
    Page<ProductReview> findByProductId(Integer productId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"product", "user"})
    Page<ProductReview> findByUserId(Integer userId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"product", "user"})
    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = :productId AND pr.user.id = :userId")
    Optional<ProductReview> findByProductIdAndUserId(@Param("productId") Integer productId, @Param("userId") Integer userId);
    
    @EntityGraph(attributePaths = {"product", "user"})
    Optional<ProductReview> findById(Integer id);
    
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Integer productId);
    
    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId")
    Long countByProductId(@Param("productId") Integer productId);
    
    @Query("SELECT pr.rating, COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId GROUP BY pr.rating ORDER BY pr.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Integer productId);
}

