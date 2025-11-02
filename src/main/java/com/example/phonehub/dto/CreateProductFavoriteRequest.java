package com.example.phonehub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductFavoriteRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;
}

