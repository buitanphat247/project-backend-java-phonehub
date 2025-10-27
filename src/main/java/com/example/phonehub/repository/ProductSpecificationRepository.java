package com.example.phonehub.repository;

import com.example.phonehub.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Integer> {
    List<ProductSpecification> findByProductId(Integer productId);
    List<ProductSpecification> findByProductIdAndGroupName(Integer productId, String groupName);
}
