package com.example.phonehub.repository;

import com.example.phonehub.entity.ImageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageCategoryRepository extends JpaRepository<ImageCategory, Integer> {
    Optional<ImageCategory> findByName(String name);
    Optional<ImageCategory> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}
