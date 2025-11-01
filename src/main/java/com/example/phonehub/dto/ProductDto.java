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
public class ProductDto {
    private Integer id;
    private String name;
    private String slug;
    private String brand;
    private CategoryDto category;
    private BigDecimal price;
    private BigDecimal priceOld;
    private String discount;
    private String thumbnailImage;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductSpecificationDto> specifications;
    private List<ProductColorDto> colors;
    private List<ProductImageDto> images;
}

 