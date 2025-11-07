package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Boolean isReviewed;
    private Integer reviewId;
    private Integer reviewRating;
    private String reviewComment;
    private LocalDateTime reviewCreatedAt;
    private LocalDateTime createdAt;
}


