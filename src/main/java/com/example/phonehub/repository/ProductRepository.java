package com.example.phonehub.repository;

import com.example.phonehub.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    
    @Query("SELECT p FROM Product p WHERE p.isPublished = true")
    Page<Product> findPublishedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isPublished = true")
    Page<Product> findPublishedProductsByCategory(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.isPublished = true")
    Page<Product> findPublishedProductsByBrand(@Param("brand") String brand, Pageable pageable);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isPublished = true ORDER BY p.brand")
    java.util.List<String> findAllPublishedBrands();
}
