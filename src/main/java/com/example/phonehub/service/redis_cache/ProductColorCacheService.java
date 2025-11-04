package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.ProductColorDto;
import com.example.phonehub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductColorCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_COLORS = "product:colors:";
    private static final long CACHE_TTL_HOURS = 1;
    
    public String buildCacheKeyByProductId(Integer productId) {
        return CACHE_PREFIX_COLORS + productId;
    }
    
    public void saveColorsToCache(Integer productId, List<ProductColorDto> colors) {
        if (productId == null || colors == null) return;
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        redisService.set(buildCacheKeyByProductId(productId), colors, cacheExpireSeconds);
    }
    
    @SuppressWarnings("unchecked")
    public List<ProductColorDto> getColorsFromCache(Integer productId) {
        if (productId == null) return null;
        return redisService.get(buildCacheKeyByProductId(productId), List.class);
    }
    
    public void removeColorsFromCache(Integer productId) {
        if (productId == null) return;
        redisService.delete(buildCacheKeyByProductId(productId));
    }
    
    public void invalidateProductColorsCache(Integer productId) {
        removeColorsFromCache(productId);
    }
}

