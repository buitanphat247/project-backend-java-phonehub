package com.example.phonehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @Column(name = "slug", unique = true, nullable = false, length = 255)
    @NotBlank(message = "Product slug is required")
    @Size(max = 255, message = "Product slug must not exceed 255 characters")
    private String slug;
    
    @Column(name = "brand", nullable = false, length = 100)
    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(name = "price_old", precision = 15, scale = 2)
    private BigDecimal priceOld;
    
    @Column(name = "discount", length = 20)
    @Size(max = 20, message = "Discount must not exceed 20 characters")
    private String discount;
    
    @Column(name = "thumbnail_image", length = 500)
    @Size(max = 500, message = "Thumbnail image URL must not exceed 500 characters")
    private String thumbnailImage;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductSpecification> specifications;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductColor> colors;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;
}
