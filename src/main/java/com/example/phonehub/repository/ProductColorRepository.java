package com.example.phonehub.repository;

import com.example.phonehub.entity.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, Integer> {
    List<ProductColor> findByProductId(Integer productId);
    Optional<ProductColor> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
