package com.example.phonehub.repository;

import com.example.phonehub.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @EntityGraph(attributePaths = {"category","createdBy","specifications","colors","images"})
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    @Override
    @EntityGraph(attributePaths = {"category","createdBy","specifications","colors","images"})
    Optional<Product> findById(Integer id);
    @EntityGraph(attributePaths = {"category"})
    @Override
    org.springframework.data.domain.Page<Product> findAll(org.springframework.data.domain.Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.isPublished = true")
    Page<Product> findPublishedProducts(Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isPublished = true")
    Page<Product> findPublishedProductsByCategory(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.isPublished = true")
    Page<Product> findPublishedProductsByBrand(@Param("brand") String brand, Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.category.id = :categoryId AND p.isPublished = true")
    Page<Product> findPublishedProductsByBrandAndCategory(@Param("brand") String brand, @Param("categoryId") Integer categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isPublished = true")
    Page<Product> findPublishedProductsByName(@Param("name") String name, Pageable pageable);
    
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.category.id = :categoryId AND p.isPublished = true")
    Page<Product> findPublishedProductsByNameAndCategory(@Param("name") String name, @Param("categoryId") Integer categoryId, Pageable pageable);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isPublished = true ORDER BY p.brand")
    java.util.List<String> findAllPublishedBrands();
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.category.id = :categoryId AND p.isPublished = true ORDER BY p.brand")
    java.util.List<String> findAllPublishedBrandsByCategoryId(@Param("categoryId") Integer categoryId);
}
