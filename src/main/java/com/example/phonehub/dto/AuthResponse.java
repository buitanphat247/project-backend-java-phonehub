package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho authentication APIs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String address;
    private String roleName;
    
    public AuthResponse(Integer id, String username, String email, String roleName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roleName = roleName;
    }
}

