package com.example.phonehub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;
    
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;
    
    @Column(name = "email", unique = true, length = 100)
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Column(name = "phone", length = 20)
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Column(name = "address", length = 255)
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Column(name = "avatar", length = 255)
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatar;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "points", nullable = false)
    private Integer points = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id")
    private UserRank rank;
    
    @Column(name = "rank_id", insertable = false, updatable = false)
    private Integer rankId;
    
    @Column(name = "refresh_token", length = 255)
    @Size(max = 255, message = "Refresh token must not exceed 255 characters")
    private String refreshToken;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Column(name = "role_id", insertable = false, updatable = false)
    private Integer roleId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> createdCategories;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> createdProducts;
}
