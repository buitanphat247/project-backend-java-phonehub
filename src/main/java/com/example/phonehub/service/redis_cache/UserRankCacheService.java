package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.UserRankDto;
import com.example.phonehub.entity.UserRank;
import com.example.phonehub.service.RedisService;
import com.example.phonehub.utils.UserRankUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * ⚡ Service quản lý Redis Cache cho UserRank
 * 
 * Chứa tất cả logic liên quan đến cache rank:
 * - Tạo cache keys
 * - Lưu rank vào cache
 * - Lấy rank từ cache
 * - Xóa rank khỏi cache
 * - Cache-aside pattern
 */
@Service
public class UserRankCacheService {

    @Autowired
    private RedisService redisService;
    
    // ========== ⚡ CACHE CONFIGURATION ==========
    private static final String CACHE_PREFIX_RANK = "rank:";
    private static final String CACHE_PREFIX_RANK_NAME = "rank:name:";
    private static final String CACHE_PREFIX_RANK_POINTS = "rank:points:";
    private static final String CACHE_KEY_ALL_RANKS = "rank:all"; // Cache cho danh sách tất cả ranks
    private static final long CACHE_TTL_HOURS = 2; // Cache hết hạn sau 2 giờ (rank ít thay đổi hơn user)
    
    // ========== 🎯 BUILD CACHE KEYS ==========
    
    /**
     * 🎯 Tạo Redis cache key cho rank theo ID
     * Format: "rank:123"
     * 
     * @param rankId ID của rank
     * @return Cache key dạng string
     */
    public String buildCacheKeyById(Integer rankId) {
        return CACHE_PREFIX_RANK + rankId;
    }
    
    /**
     * 🎯 Tạo Redis cache key cho rank theo tên
     * Format: "rank:name:bronze" (lowercase)
     * 
     * @param name Tên của rank
     * @return Cache key dạng string
     */
    public String buildCacheKeyByName(String name) {
        return CACHE_PREFIX_RANK_NAME + name.toLowerCase();
    }
    
    /**
     * 🎯 Tạo Redis cache key cho rank theo điểm số
     * Format: "rank:points:100"
     * 
     * @param points Điểm số
     * @return Cache key dạng string
     */
    public String buildCacheKeyByPoints(Integer points) {
        return CACHE_PREFIX_RANK_POINTS + points;
    }
    
    // ========== 💾 SAVE TO CACHE ==========
    
    /**
     * 💾 Lưu rank vào Redis cache với nhiều keys (ID, name)
     * 
     * @param rankDto RankDto cần lưu vào cache
     */
    public void saveRankToCache(UserRankDto rankDto) {
        if (rankDto == null || rankDto.getId() == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        
        // Cache theo ID
        String cacheKeyById = buildCacheKeyById(rankDto.getId());
        redisService.set(cacheKeyById, rankDto, cacheExpireSeconds);
        
        // Cache theo name (nếu có)
        if (rankDto.getName() != null && !rankDto.getName().trim().isEmpty()) {
            String cacheKeyByName = buildCacheKeyByName(rankDto.getName());
            redisService.set(cacheKeyByName, rankDto, cacheExpireSeconds);
        }
    }
    
    /**
     * 💾 Lưu danh sách tất cả ranks vào cache
     * 
     * @param ranks List<UserRankDto> cần lưu vào cache
     */
    public void saveAllRanksToCache(List<UserRankDto> ranks) {
        if (ranks == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        redisService.set(CACHE_KEY_ALL_RANKS, ranks, cacheExpireSeconds);
    }
    
    /**
     * 💾 Lưu rank vào cache theo điểm số (để tìm nhanh theo points)
     * 
     * @param points Điểm số
     * @param rankDto RankDto tương ứng với điểm số
     */
    public void saveRankByPointsToCache(Integer points, UserRankDto rankDto) {
        if (points == null || rankDto == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        String cacheKey = buildCacheKeyByPoints(points);
        redisService.set(cacheKey, rankDto, cacheExpireSeconds);
    }
    
    // ========== 🔍 GET FROM CACHE ==========
    
    /**
     * 🔍 Lấy rank từ cache theo ID
     * 
     * @param rankId ID của rank
     * @return UserRankDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserRankDto getRankFromCacheById(Integer rankId) {
        if (rankId == null) return null;
        return redisService.get(buildCacheKeyById(rankId), UserRankDto.class);
    }
    
    /**
     * 🔍 Lấy rank từ cache theo tên
     * 
     * @param name Tên của rank
     * @return UserRankDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserRankDto getRankFromCacheByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return redisService.get(buildCacheKeyByName(name), UserRankDto.class);
    }
    
    /**
     * 🔍 Lấy rank từ cache theo điểm số
     * 
     * @param points Điểm số
     * @return UserRankDto nếu tìm thấy trong cache, null nếu không có
     */
    public UserRankDto getRankFromCacheByPoints(Integer points) {
        if (points == null) return null;
        return redisService.get(buildCacheKeyByPoints(points), UserRankDto.class);
    }
    
    /**
     * 🔍 Lấy danh sách tất cả ranks từ cache
     * 
     * @return List<UserRankDto> nếu tìm thấy trong cache, null nếu không có
     */
    @SuppressWarnings("unchecked")
    public List<UserRankDto> getAllRanksFromCache() {
        Object cached = redisService.get(CACHE_KEY_ALL_RANKS);
        if (cached instanceof List) {
            return (List<UserRankDto>) cached;
        }
        return null;
    }
    
    // ========== 🗑️ REMOVE FROM CACHE ==========
    
    /**
     * 🗑️ Xóa rank khỏi Redis cache (tất cả keys liên quan)
     * 
     * Khi rank bị xóa hoặc update, cần invalidate cache để đảm bảo data consistency
     * 
     * @param rankId ID của rank (required)
     * @param name Tên của rank (optional, null nếu không có)
     */
    public void removeRankFromCache(Integer rankId, String name) {
        // Xóa cache theo ID
        if (rankId != null) {
            redisService.delete(buildCacheKeyById(rankId));
        }
        
        // Xóa cache theo name (nếu có)
        if (name != null && !name.trim().isEmpty()) {
            redisService.delete(buildCacheKeyByName(name));
        }
        
        // Xóa cache danh sách tất cả ranks (vì đã thay đổi)
        redisService.delete(CACHE_KEY_ALL_RANKS);
        
        // Lưu ý: Không xóa cache theo points vì có thể có nhiều điểm số khác nhau
        // map đến cùng 1 rank, nên để tự động expire hoặc invalidate khi cần
    }
    
    /**
     * 🗑️ Xóa tất cả cache liên quan đến ranks (khi có thay đổi lớn)
     */
    public void invalidateAllRankCache() {
        // Xóa cache danh sách tất cả ranks
        redisService.delete(CACHE_KEY_ALL_RANKS);
        
        // Lưu ý: Có thể xóa tất cả keys "rank:*" nếu cần, nhưng tốn performance
        // Nên để tự động expire hoặc invalidate từng cái khi update/delete
    }
    
    // ========== 🎁 CACHE-ASIDE PATTERN ==========
    
    /**
     * 🎁 Pattern chung: Lấy rank từ cache, nếu không có thì lấy từ DB và cache lại
     * 
     * Cache-aside pattern (Lazy Loading):
     * 1. Check cache → Nếu có → Return ngay (FAST ⚡)
     * 2. Nếu không có → Query database
     * 3. Convert UserRank → UserRankDto
     * 4. Lưu kết quả vào cache → Return (chậm hơn lần đầu, nhưng lần sau sẽ nhanh)
     * 
     * @param cacheValue UserRankDto từ cache (null nếu không có)
     * @param dbQuery Lambda function để query từ database
     * @return Optional<UserRankDto>
     */
    public Optional<UserRankDto> getRankWithCacheStrategy(
            UserRankDto cacheValue,
            Supplier<Optional<UserRank>> dbQuery) {
        
        // ✅ Bước 1: Nếu có trong cache → Return ngay (CACHE HIT - nhanh nhất)
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        
        // ❌ Bước 2: Không có trong cache → Query từ database (CACHE MISS)
        Optional<UserRank> rankFromDb = dbQuery.get();
        
        // Bước 3: Convert UserRank → UserRankDto
        Optional<UserRankDto> rankDto = rankFromDb.map(UserRankUtils::toDto);
        
        // Bước 4: Lưu vào cache để lần sau nhanh hơn
        rankDto.ifPresent(this::saveRankToCache);
        
        return rankDto;
    }
}

