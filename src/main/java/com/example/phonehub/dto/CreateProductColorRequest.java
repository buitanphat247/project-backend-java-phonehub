package com.example.phonehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductColorRequest {
    @NotNull private Integer productId;
    @NotBlank @Size(max = 50) private String name;
    @Size(max = 10) private String hexColor;
}

 