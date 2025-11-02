package com.example.phonehub.repository;

import com.example.phonehub.entity.ProductFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Integer> {
    
    @EntityGraph(attributePaths = {"user", "product", "product.category"})
    @Query("SELECT pf FROM ProductFavorite pf WHERE pf.user.id = :userId")
    Page<ProductFavorite> findByUserId(@Param("userId") Integer userId, Pageable pageable);
    
    @Query("SELECT pf FROM ProductFavorite pf WHERE pf.user.id = :userId AND pf.product.id = :productId")
    Optional<ProductFavorite> findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
    
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.product.id = :productId")
    long countByProductId(@Param("productId") Integer productId);
}

