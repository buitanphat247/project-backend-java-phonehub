package com.example.phonehub.utils;

import com.example.phonehub.entity.User;
import com.example.phonehub.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {
    
    // Convert Entity to DTO
    public static UserDto toDto(User user) {
        if (user == null) return null;
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatar(user.getAvatar());
        dto.setBirthday(user.getBirthday());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Convert role
        if (user.getRole() != null) {
            dto.setRole(RoleUtils.toDto(user.getRole()));
        }
        
        return dto;
    }
    
    // Convert DTO to Entity
    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setAvatar(dto.getAvatar());
        user.setBirthday(dto.getBirthday());
        return user;
    }
    
    // Convert List Entity to List DTO
    public static List<UserDto> toDtoList(List<User> users) {
        if (users == null) return null;
        
        return users.stream()
                .map(UserUtils::toDto)
                .collect(Collectors.toList());
    }
    
    // Convert Page Entity to Page DTO
    public static Page<UserDto> toDtoPage(Page<User> userPage) {
        if (userPage == null) return null;
        
        List<UserDto> dtoList = toDtoList(userPage.getContent());
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }
}
