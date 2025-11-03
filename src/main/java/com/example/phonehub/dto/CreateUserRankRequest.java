package com.example.phonehub.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class CreateUserRankRequest {
    @NotBlank(message = "Rank name is required")
    @Size(max = 100, message = "Rank name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Min points is required")
    private Integer minPoints;
    
    @NotNull(message = "Max points is required")
    private Integer maxPoints;
    
    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount must be at least 0.00")
    private BigDecimal discount = BigDecimal.ZERO;
}

