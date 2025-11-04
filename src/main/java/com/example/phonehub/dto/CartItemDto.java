package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Integer id;
    private Integer productId;
    private ProductDto product; // sản phẩm tổng quan (summary)
    private Integer quantity;
    private BigDecimal priceAtAdd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


