package com.example.phonehub.service.redis_cache;

import com.example.phonehub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_REVIEW_STATS = "product:review:stats:";
    
    public String buildCacheKeyForStats(Integer productId) {
        return CACHE_PREFIX_REVIEW_STATS + productId;
    }
    
    public void invalidateProductReviewCache(Integer productId) {
        if (productId == null) return;
        redisService.delete(buildCacheKeyForStats(productId));
    }
    
    public void invalidateUserReviewCache(Integer userId) {
    }
}

