package com.example.phonehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank @Size(max = 255) private String name;
    @Size(max = 255) private String slug;
    @NotBlank @Size(max = 100) private String brand;
    @NotNull private Integer categoryId;
    private BigDecimal price;
    private BigDecimal priceOld;
    private String discount;
    private String thumbnailImage;
    private Boolean isPublished;
}

 