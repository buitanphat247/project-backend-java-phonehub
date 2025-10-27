package com.example.phonehub.utils;

import com.example.phonehub.entity.Category;
import com.example.phonehub.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryUtils {
    
    // Convert Entity to DTO
    public static CategoryDto toDto(Category category) {
        if (category == null) return null;
        
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Convert createdBy user
        if (category.getCreatedBy() != null) {
            dto.setCreatedBy(UserUtils.toDto(category.getCreatedBy()));
        }
        
        return dto;
    }
    
    // Convert DTO to Entity
    public static Category toEntity(CategoryDto dto) {
        if (dto == null) return null;
        
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        return category;
    }
    
    // Convert List Entity to List DTO
    public static List<CategoryDto> toDtoList(List<Category> categories) {
        if (categories == null) return null;
        
        return categories.stream()
                .map(CategoryUtils::toDto)
                .collect(Collectors.toList());
    }
    
    // Convert Page Entity to Page DTO
    public static Page<CategoryDto> toDtoPage(Page<Category> categoryPage) {
        if (categoryPage == null) return null;
        
        List<CategoryDto> dtoList = toDtoList(categoryPage.getContent());
        return new PageImpl<>(dtoList, categoryPage.getPageable(), categoryPage.getTotalElements());
    }
}
