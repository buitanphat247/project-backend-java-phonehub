package com.example.phonehub.repository;

import com.example.phonehub.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUser_Id(Integer userId, Pageable pageable);
}


