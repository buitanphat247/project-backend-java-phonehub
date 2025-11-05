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
    private Integer userId;

    @NotBlank
    @Size(max = 100)
    private String buyerName;

    @Size(max = 100)
    private String buyerEmail;

    @Size(max = 20)
    private String buyerPhone;

    @Size(max = 255)
    private String buyerAddress;

    @Size(max = 50)
    private String paymentMethod = "COD";

    @NotNull
    private List<CreateOrderItemRequest> items;
}


