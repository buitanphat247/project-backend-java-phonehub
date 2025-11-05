package com.example.phonehub.repository;

import com.example.phonehub.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUser_Id(Integer userId, Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findByUser_IdAndStatus(Integer userId, String status, Pageable pageable);

    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.user.id = :userId and o.status = 'success'")
    BigDecimal sumTotalSpentByUser(@Param("userId") Integer userId);
}


