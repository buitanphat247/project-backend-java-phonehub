package com.example.phonehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_specifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "group_name", nullable = false, length = 100)
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;
    
    @Column(name = "label", nullable = false, length = 255)
    @NotBlank(message = "Label is required")
    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label;
    
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;
    
    @Column(name = "type", nullable = false, length = 20)
    @NotBlank(message = "Type is required")
    @Size(max = 20, message = "Type must not exceed 20 characters")
    private String type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
