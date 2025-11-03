package com.example.phonehub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRankRequest {
    // Tất cả fields đều optional - chỉ validate khi có giá trị
    
    @Size(max = 100, message = "Rank name must not exceed 100 characters")
    private String name;
    
    private Integer minPoints;
    
    private Integer maxPoints;
    
    @DecimalMin(value = "0.00", message = "Discount must be at least 0.00")
    private BigDecimal discount;
}

