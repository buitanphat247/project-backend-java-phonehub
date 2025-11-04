package com.example.phonehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull
    private Integer userId;

    @NotBlank
    @Size(max = 50)
    private String paymentMethod; // COD, BANKING, ...

    @Size(max = 255)
    private String note;

    @NotNull
    private List<CreateOrderItemRequest> items;
}


