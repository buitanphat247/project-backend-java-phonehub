package com.example.phonehub.utils;

import com.example.phonehub.entity.Role;
import com.example.phonehub.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class RoleUtils {
    
    // Convert Entity to DTO
    public static RoleDto toDto(Role role) {
        if (role == null) return null;
        
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
    
    // Convert DTO to Entity
    public static Role toEntity(RoleDto dto) {
        if (dto == null) return null;
        
        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        return role;
    }
    
    // Convert List Entity to List DTO
    public static List<RoleDto> toDtoList(List<Role> roles) {
        if (roles == null) return null;
        
        return roles.stream()
                .map(RoleUtils::toDto)
                .collect(Collectors.toList());
    }
    
    // Convert Page Entity to Page DTO
    public static Page<RoleDto> toDtoPage(Page<Role> rolePage) {
        if (rolePage == null) return null;
        
        List<RoleDto> dtoList = toDtoList(rolePage.getContent());
        return new PageImpl<>(dtoList, rolePage.getPageable(), rolePage.getTotalElements());
    }
}
