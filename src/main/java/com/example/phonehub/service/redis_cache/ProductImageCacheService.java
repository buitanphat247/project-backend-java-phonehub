package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.ProductImageDto;
import com.example.phonehub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_IMAGES = "product:images:";
    private static final long CACHE_TTL_HOURS = 1;
    
    public String buildCacheKeyByProductId(Integer productId) {
        return CACHE_PREFIX_IMAGES + productId;
    }
    
    public void saveImagesToCache(Integer productId, List<ProductImageDto> images) {
        if (productId == null || images == null) return;
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        redisService.set(buildCacheKeyByProductId(productId), images, cacheExpireSeconds);
    }
    
    @SuppressWarnings("unchecked")
    public List<ProductImageDto> getImagesFromCache(Integer productId) {
        if (productId == null) return null;
        return redisService.get(buildCacheKeyByProductId(productId), List.class);
    }
    
    public void removeImagesFromCache(Integer productId) {
        if (productId == null) return;
        redisService.delete(buildCacheKeyByProductId(productId));
    }
    
    public void invalidateProductImagesCache(Integer productId) {
        removeImagesFromCache(productId);
    }
}

