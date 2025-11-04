package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.ProductSpecificationDto;
import com.example.phonehub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSpecificationCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_SPECS = "product:specs:";
    private static final String CACHE_PREFIX_SPECS_GROUP = "product:specs:group:";
    private static final long CACHE_TTL_HOURS = 1;
    
    public String buildCacheKeyByProductId(Integer productId) {
        return CACHE_PREFIX_SPECS + productId;
    }
    
    public String buildCacheKeyByProductAndGroup(Integer productId, String group) {
        return CACHE_PREFIX_SPECS_GROUP + productId + ":" + group.toLowerCase();
    }
    
    public void saveSpecsToCache(Integer productId, List<ProductSpecificationDto> specs) {
        if (productId == null || specs == null) return;
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        redisService.set(buildCacheKeyByProductId(productId), specs, cacheExpireSeconds);
    }
    
    public void saveSpecsByGroupToCache(Integer productId, String group, List<ProductSpecificationDto> specs) {
        if (productId == null || group == null || specs == null) return;
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        redisService.set(buildCacheKeyByProductAndGroup(productId, group), specs, cacheExpireSeconds);
    }
    
    @SuppressWarnings("unchecked")
    public List<ProductSpecificationDto> getSpecsFromCache(Integer productId) {
        if (productId == null) return null;
        return redisService.get(buildCacheKeyByProductId(productId), List.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<ProductSpecificationDto> getSpecsByGroupFromCache(Integer productId, String group) {
        if (productId == null || group == null) return null;
        return redisService.get(buildCacheKeyByProductAndGroup(productId, group), List.class);
    }
    
    public void removeSpecsFromCache(Integer productId) {
        if (productId == null) return;
        redisService.delete(buildCacheKeyByProductId(productId));
    }
    
    public void removeSpecsByGroupFromCache(Integer productId, String group) {
        if (productId == null || group == null) return;
        redisService.delete(buildCacheKeyByProductAndGroup(productId, group));
    }
    
    public void invalidateProductSpecsCache(Integer productId) {
        removeSpecsFromCache(productId);
    }
}

