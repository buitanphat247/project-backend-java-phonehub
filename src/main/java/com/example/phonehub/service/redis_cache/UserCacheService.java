package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.UserDto;
import com.example.phonehub.entity.User;
import com.example.phonehub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ⚡ Service quản lý Redis Cache cho User
 * 
 * Chứa tất cả logic liên quan đến cache user:
 * - Tạo cache keys
 * - Lưu user vào cache
 * - Lấy user từ cache
 * - Xóa user khỏi cache
 * - Cache-aside pattern
 */
@Service
public class UserCacheService {

    @Autowired
    private RedisService redisService;
    
    // ========== ⚡ CACHE CONFIGURATION ==========
    private static final String CACHE_PREFIX_USER = "user:";
    private static final String CACHE_PREFIX_USERNAME = "user:username:";
    private static final String CACHE_PREFIX_EMAIL = "user:email:";
    private static final long CACHE_TTL_HOURS = 1; // Cache hết hạn sau 1 giờ
    
    // ========== 🎯 BUILD CACHE KEYS ==========
    
    /**
     * 🎯 Tạo Redis cache key cho user theo ID
     * Format: "user:123"
     * 
     * @param userId ID của user
     * @return Cache key dạng string
     */
    public String buildCacheKeyById(Integer userId) {
        return CACHE_PREFIX_USER + userId;
    }
    
    /**
     * 🎯 Tạo Redis cache key cho user theo username
     * Format: "user:username:john_doe" (lowercase để tránh case-sensitive)
     * 
     * @param username Username của user
     * @return Cache key dạng string
     */
    public String buildCacheKeyByUsername(String username) {
        return CACHE_PREFIX_USERNAME + username.toLowerCase();
    }
    
    /**
     * 🎯 Tạo Redis cache key cho user theo email
     * Format: "user:email:john@example.com" (lowercase để tránh case-sensitive)
     * 
     * @param email Email của user
     * @return Cache key dạng string
     */
    public String buildCacheKeyByEmail(String email) {
        return CACHE_PREFIX_EMAIL + email.toLowerCase();
    }
    
    // ========== 💾 SAVE TO CACHE ==========
    
    /**
     * 💾 Lưu user vào Redis cache với nhiều keys (ID, username, email)
     * 
     * Chiến lược: Cache-aside pattern với multi-key indexing
     * - Cache theo ID: "user:123" → UserDto
     * - Cache theo username: "user:username:john_doe" → UserDto  
     * - Cache theo email: "user:email:john@example.com" → UserDto
     * 
     * Lợi ích: Tìm được user theo bất kỳ cách nào (ID, username, email) đều hit cache
     * 
     * @param userDto UserDto cần lưu vào cache
     */
    public void saveUserToCache(UserDto userDto) {
        // Kiểm tra input hợp lệ
        if (userDto == null || userDto.getId() == null) {
            return; // Không có gì để cache
        }
        
        // Tính TTL (Time To Live) - thời gian cache sống
        // Ví dụ: 1 giờ = 3600 giây
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        
        // 📌 Cache theo ID (primary key)
        String cacheKeyById = buildCacheKeyById(userDto.getId());
        redisService.set(cacheKeyById, userDto, cacheExpireSeconds);
        
        // 📌 Cache theo username (nếu có) - để tìm nhanh theo username
        String username = userDto.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            String cacheKeyByUsername = buildCacheKeyByUsername(username);
            redisService.set(cacheKeyByUsername, userDto, cacheExpireSeconds);
        }
        
        // 📌 Cache theo email (nếu có) - để tìm nhanh theo email
        String email = userDto.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            String cacheKeyByEmail = buildCacheKeyByEmail(email);
            redisService.set(cacheKeyByEmail, userDto, cacheExpireSeconds);
        }
    }
    
    // ========== 🔍 GET FROM CACHE ==========
    
    /**
     * 🔍 Lấy user từ Redis cache theo ID
     * 
     * @param userId ID của user
     * @return UserDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserDto getUserFromCacheById(Integer userId) {
        if (userId == null) return null;
        
        String cacheKey = buildCacheKeyById(userId);
        return redisService.get(cacheKey, UserDto.class);
    }
    
    /**
     * 🔍 Lấy user từ Redis cache theo username
     * 
     * @param username Username của user
     * @return UserDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserDto getUserFromCacheByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        
        String cacheKey = buildCacheKeyByUsername(username);
        return redisService.get(cacheKey, UserDto.class);
    }
    
    /**
     * 🔍 Lấy user từ Redis cache theo email
     * 
     * @param email Email của user
     * @return UserDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserDto getUserFromCacheByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        
        String cacheKey = buildCacheKeyByEmail(email);
        return redisService.get(cacheKey, UserDto.class);
    }
    
    // ========== 🗑️ REMOVE FROM CACHE ==========
    
    /**
     * 🗑️ Xóa user khỏi Redis cache (tất cả keys liên quan)
     * 
     * Khi user bị xóa hoặc update, cần invalidate cache để đảm bảo data consistency
     * 
     * @param userId ID của user (required)
     * @param username Username của user (optional, null nếu không có)
     * @param email Email của user (optional, null nếu không có)
     */
    public void removeUserFromCache(Integer userId, String username, String email) {
        // Xóa cache theo ID
        if (userId != null) {
            String cacheKeyById = buildCacheKeyById(userId);
            redisService.delete(cacheKeyById);
        }
        
        // Xóa cache theo username (nếu có)
        if (username != null && !username.trim().isEmpty()) {
            String cacheKeyByUsername = buildCacheKeyByUsername(username);
            redisService.delete(cacheKeyByUsername);
        }
        
        // Xóa cache theo email (nếu có)
        if (email != null && !email.trim().isEmpty()) {
            String cacheKeyByEmail = buildCacheKeyByEmail(email);
            redisService.delete(cacheKeyByEmail);
        }
    }
    
    // ========== 🎁 CACHE-ASIDE PATTERN ==========
    
    /**
     * 🎁 Pattern chung: Lấy user từ cache, nếu không có thì lấy từ DB và cache lại
     * 
     * Cache-aside pattern (Lazy Loading):
     * 1. Check cache → Nếu có → Return ngay (FAST ⚡)
     * 2. Nếu không có → Query database
     * 3. Convert User → UserDto
     * 4. Lưu kết quả vào cache → Return (chậm hơn lần đầu, nhưng lần sau sẽ nhanh)
     * 
     * @param cacheValue UserDto từ cache (null nếu không có)
     * @param dbQuery Lambda function để query từ database
     * @param toDtoConverter Lambda function để convert User → UserDto
     * @return Optional<UserDto>
     */
    public Optional<UserDto> getUserWithCacheStrategy(
            UserDto cacheValue,
            Supplier<Optional<User>> dbQuery,
            Function<User, UserDto> toDtoConverter) {
        
        // ✅ Bước 1: Nếu có trong cache → Return ngay (CACHE HIT - nhanh nhất)
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        
        // ❌ Bước 2: Không có trong cache → Query từ database (CACHE MISS)
        Optional<User> userFromDb = dbQuery.get();
        
        // Bước 3: Convert User → UserDto
        Optional<UserDto> userDto = userFromDb.map(toDtoConverter);
        
        // Bước 4: Lưu vào cache để lần sau nhanh hơn
        userDto.ifPresent(this::saveUserToCache);
        
        return userDto;
    }
}

