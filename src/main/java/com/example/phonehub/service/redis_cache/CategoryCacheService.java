package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.CategoryDto;
import com.example.phonehub.entity.Category;
import com.example.phonehub.service.RedisService;
import com.example.phonehub.utils.CategoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class CategoryCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_CATEGORY = "category:";
    private static final String CACHE_PREFIX_CATEGORY_SLUG = "category:slug:";
    private static final long CACHE_TTL_HOURS = 2;
    
    public String buildCacheKeyById(Integer categoryId) {
        return CACHE_PREFIX_CATEGORY + categoryId;
    }
    
    public String buildCacheKeyBySlug(String slug) {
        return CACHE_PREFIX_CATEGORY_SLUG + slug.toLowerCase();
    }
    
    public void saveCategoryToCache(CategoryDto categoryDto) {
        if (categoryDto == null || categoryDto.getId() == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        
        String cacheKeyById = buildCacheKeyById(categoryDto.getId());
        redisService.set(cacheKeyById, categoryDto, cacheExpireSeconds);
        
        if (categoryDto.getSlug() != null && !categoryDto.getSlug().trim().isEmpty()) {
            String cacheKeyBySlug = buildCacheKeyBySlug(categoryDto.getSlug());
            redisService.set(cacheKeyBySlug, categoryDto, cacheExpireSeconds);
        }
    }
    
    public CategoryDto getCategoryFromCacheById(Integer categoryId) {
        if (categoryId == null) return null;
        return redisService.get(buildCacheKeyById(categoryId), CategoryDto.class);
    }
    
    public CategoryDto getCategoryFromCacheBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) return null;
        return redisService.get(buildCacheKeyBySlug(slug), CategoryDto.class);
    }
    
    public void removeCategoryFromCache(Integer categoryId, String slug) {
        if (categoryId != null) {
            redisService.delete(buildCacheKeyById(categoryId));
        }
        
        if (slug != null && !slug.trim().isEmpty()) {
            redisService.delete(buildCacheKeyBySlug(slug));
        }
    }
    
    public Optional<CategoryDto> getCategoryWithCacheStrategy(
            CategoryDto cacheValue,
            Supplier<Optional<Category>> dbQuery) {
        
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        
        Optional<Category> categoryFromDb = dbQuery.get();
        Optional<CategoryDto> categoryDto = categoryFromDb.map(CategoryUtils::toDto);
        
        categoryDto.ifPresent(this::saveCategoryToCache);
        
        return categoryDto;
    }
}

