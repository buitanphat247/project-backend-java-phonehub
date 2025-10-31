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
public class CreateProductSpecificationRequest {
    @NotNull private Integer productId;
    @NotBlank @Size(max = 100) private String groupName;
    @NotBlank @Size(max = 255) private String label;
    private String value;
    @NotBlank @Size(max = 20) private String type;
}

 