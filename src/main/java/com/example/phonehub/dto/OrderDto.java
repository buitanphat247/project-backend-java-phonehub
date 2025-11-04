package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Integer id;
    private Integer userId;
    private String username;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> items;
}


