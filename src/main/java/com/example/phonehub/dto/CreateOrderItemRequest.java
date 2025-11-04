package com.example.phonehub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequest {
    @NotNull
    private Integer productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}


