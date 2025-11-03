package com.example.phonehub.utils;

import com.example.phonehub.entity.User;
import com.example.phonehub.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {
    
    // Convert Entity to DTO (basic - chỉ lấy ID của role và rank, không load full object)
    public static UserDto toDtoBasic(User user) {
        if (user == null) return null;
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatar(user.getAvatar());
        dto.setBirthday(user.getBirthday());
        dto.setPoints(user.getPoints());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Lấy ID của role và rank (ưu tiên từ foreign key column, nếu null thì lấy từ relationship)
        if (user.getRoleId() != null) {
            dto.setRoleId(user.getRoleId());
        } else {
            try {
                if (user.getRole() != null) {
                    dto.setRoleId(user.getRole().getId());
                }
            } catch (Exception e) {
                // Lazy loading exception - ignore
            }
        }
        
        if (user.getRankId() != null) {
            dto.setRankId(user.getRankId());
        } else {
            try {
                if (user.getRank() != null) {
                    dto.setRankId(user.getRank().getId());
                }
            } catch (Exception e) {
                // Lazy loading exception - ignore
            }
        }
        
        return dto;
    }
    
    // Convert Entity to DTO (chỉ lấy ID của role và rank - cho danh sách)
    public static UserDto toDto(User user) {
        return toDtoBasic(user); // UserDto chỉ có roleId và rankId, không có full object
    }
    
    // Convert Entity to DTO (full - có cả role và rank objects - cho chi tiết user)
    public static UserDto toDtoFull(User user) {
        if (user == null) return null;
        
        UserDto dto = toDtoBasic(user); // Lấy thông tin cơ bản + roleId, rankId
        
        // Convert role full object (chỉ khi đã được load)
        try {
            if (user.getRole() != null) {
                dto.setRole(RoleUtils.toDto(user.getRole()));
            }
        } catch (Exception e) {
            // Lazy loading exception - ignore
        }
        
        // Convert rank full object (chỉ khi đã được load)
        try {
            if (user.getRank() != null) {
                dto.setRank(UserRankUtils.toDto(user.getRank()));
            }
        } catch (Exception e) {
            // Lazy loading exception - ignore
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
        user.setPoints(dto.getPoints());
        return user;
    }
    
    // Convert List Entity to List DTO
    public static List<UserDto> toDtoList(List<User> users) {
        if (users == null) return null;
        
        return users.stream()
                .map(UserUtils::toDto)
                .collect(Collectors.toList());
    }
    
    // Convert Page Entity to Page DTO (full - có role và rank)
    public static Page<UserDto> toDtoPage(Page<User> userPage) {
        if (userPage == null) return null;
        
        List<UserDto> dtoList = toDtoList(userPage.getContent());
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }
    
    // Convert Page Entity to Page DTO (basic - không có role và rank)
    public static Page<UserDto> toDtoPageBasic(Page<User> userPage) {
        if (userPage == null) return null;
        
        List<UserDto> dtoList = userPage.getContent().stream()
                .map(UserUtils::toDtoBasic)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }
}
