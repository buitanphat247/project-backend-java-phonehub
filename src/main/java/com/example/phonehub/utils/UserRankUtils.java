package com.example.phonehub.utils;

import com.example.phonehub.dto.UserRankDto;
import com.example.phonehub.entity.UserRank;

import java.util.List;
import java.util.stream.Collectors;

public class UserRankUtils {
    
    public static UserRankDto toDto(UserRank ur) {
        if (ur == null) return null;
        
        UserRankDto dto = new UserRankDto();
        dto.setId(ur.getId());
        dto.setName(ur.getName());
        dto.setMinPoints(ur.getMinPoints());
        dto.setMaxPoints(ur.getMaxPoints());
        dto.setDiscount(ur.getDiscount());
        dto.setCreatedAt(ur.getCreatedAt());
        dto.setUpdatedAt(ur.getUpdatedAt());
        
        return dto;
    }
    
    public static List<UserRankDto> toDtoList(List<UserRank> list) {
        if (list == null) return null;
        return list.stream().map(UserRankUtils::toDto).collect(Collectors.toList());
    }
}

