package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.ProductDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.service.RedisService;
import com.example.phonehub.utils.ProductUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class ProductCacheService {

    @Autowired
    private RedisService redisService;
    
    private static final String CACHE_PREFIX_PRODUCT = "product:";
    private static final String CACHE_PREFIX_PRODUCT_SLUG = "product:slug:";
    private static final long CACHE_TTL_HOURS = 1;
    
    public String buildCacheKeyById(Integer productId) {
        return CACHE_PREFIX_PRODUCT + productId;
    }
    
    public String buildCacheKeyBySlug(String slug) {
        return CACHE_PREFIX_PRODUCT_SLUG + slug.toLowerCase();
    }
    
    public void saveProductToCache(ProductDto productDto) {
        if (productDto == null || productDto.getId() == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        
        String cacheKeyById = buildCacheKeyById(productDto.getId());
        redisService.set(cacheKeyById, productDto, cacheExpireSeconds);
        
        if (productDto.getSlug() != null && !productDto.getSlug().trim().isEmpty()) {
            String cacheKeyBySlug = buildCacheKeyBySlug(productDto.getSlug());
            redisService.set(cacheKeyBySlug, productDto, cacheExpireSeconds);
        }
    }
    
    public ProductDto getProductFromCacheById(Integer productId) {
        if (productId == null) return null;
        return redisService.get(buildCacheKeyById(productId), ProductDto.class);
    }
    
    public ProductDto getProductFromCacheBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) return null;
        return redisService.get(buildCacheKeyBySlug(slug), ProductDto.class);
    }
    
    public void removeProductFromCache(Integer productId, String slug) {
        if (productId != null) {
            redisService.delete(buildCacheKeyById(productId));
        }
        
        if (slug != null && !slug.trim().isEmpty()) {
            redisService.delete(buildCacheKeyBySlug(slug));
        }
    }
    
    public Optional<ProductDto> getProductWithCacheStrategy(
            ProductDto cacheValue,
            Supplier<Optional<Product>> dbQuery) {
        
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        
        Optional<Product> productFromDb = dbQuery.get();
        Optional<ProductDto> productDto = productFromDb.map(ProductUtils::toDto);
        
        productDto.ifPresent(this::saveProductToCache);
        
        return productDto;
    }
}

