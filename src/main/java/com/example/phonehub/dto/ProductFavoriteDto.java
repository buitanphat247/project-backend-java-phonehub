package com.example.phonehub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductFavoriteDto {
    private Integer id;
    private Integer userId;
    private UserDto user;
    private Integer productId;
    private ProductDto product;
    private LocalDateTime createdAt;
}

