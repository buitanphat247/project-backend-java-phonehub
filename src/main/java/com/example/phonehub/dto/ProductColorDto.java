package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductColorDto {
    private Integer id;
    private Integer productId;
    private String name;
    private String hexColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

 