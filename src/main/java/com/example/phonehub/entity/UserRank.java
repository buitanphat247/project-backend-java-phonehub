package com.example.phonehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_ranks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Rank name is required")
    @Size(max = 100, message = "Rank name must not exceed 100 characters")
    private String name;
    
    @Column(name = "min_points", nullable = false)
    @NotNull(message = "Min points is required")
    private Integer minPoints;
    
    @Column(name = "max_points", nullable = false)
    @NotNull(message = "Max points is required")
    private Integer maxPoints;
    
    @Column(name = "discount", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount must be at least 0.00")
    private BigDecimal discount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

