package com.example.phonehub.service.redis_cache;

import org.springframework.stereotype.Service;

@Service
public class ProductFavoriteCacheService {
    
    public void invalidateUserFavoritesCache(Integer userId) {
    }
    
    public void invalidateProductFavoriteCache(Integer productId) {
    }
}

