package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Integer id;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Category slug is required")
    @Size(max = 150, message = "Category slug must not exceed 150 characters")
    private String slug;
    
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
