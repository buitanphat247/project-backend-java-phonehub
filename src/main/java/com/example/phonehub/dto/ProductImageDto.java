package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Integer id;
    private Integer productId;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



