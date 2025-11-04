package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.RoleDto;
import com.example.phonehub.entity.Role;
import com.example.phonehub.service.RedisService;
import com.example.phonehub.utils.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * ⚡ Service quản lý Redis Cache cho Role
 * 
 * Chứa tất cả logic liên quan đến cache role:
 * - Tạo cache keys
 * - Lưu role vào cache
 * - Lấy role từ cache
 * - Xóa role khỏi cache
 * - Cache-aside pattern
 */
@Service
public class RoleCacheService {

    @Autowired
    private RedisService redisService;
    
    // ========== ⚡ CACHE CONFIGURATION ==========
    private static final String CACHE_PREFIX_ROLE = "role:";
    private static final String CACHE_PREFIX_ROLE_NAME = "role:name:";
    private static final String CACHE_PREFIX_ROLE_PAGE = "role:page:"; // Cache cho phân trang: "role:page:0:10"
    private static final long CACHE_TTL_HOURS = 3; // Cache hết hạn sau 3 giờ (role ít thay đổi nhất)
    
    // ========== 🎯 BUILD CACHE KEYS ==========
    
    /**
     * 🎯 Tạo Redis cache key cho role theo ID
     * Format: "role:123"
     * 
     * @param roleId ID của role
     * @return Cache key dạng string
     */
    public String buildCacheKeyById(Integer roleId) {
        return CACHE_PREFIX_ROLE + roleId;
    }
    
    /**
     * 🎯 Tạo Redis cache key cho role theo tên
     * Format: "role:name:admin" (lowercase)
     * 
     * @param name Tên của role
     * @return Cache key dạng string
     */
    public String buildCacheKeyByName(String name) {
        return CACHE_PREFIX_ROLE_NAME + name.toLowerCase();
    }
    
    /**
     * 🎯 Tạo Redis cache key cho phân trang roles
     * Format: "role:page:0:10" (page:size)
     * 
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Cache key dạng string
     */
    public String buildCacheKeyForPage(int page, int size) {
        return CACHE_PREFIX_ROLE_PAGE + page + ":" + size;
    }
    
    // ========== 💾 SAVE TO CACHE ==========
    
    /**
     * 💾 Lưu role vào Redis cache với nhiều keys (ID, name)
     * 
     * @param roleDto RoleDto cần lưu vào cache
     */
    public void saveRoleToCache(RoleDto roleDto) {
        if (roleDto == null || roleDto.getId() == null) {
            return;
        }
        
        long cacheExpireSeconds = CACHE_TTL_HOURS * 3600;
        
        // Cache theo ID
        String cacheKeyById = buildCacheKeyById(roleDto.getId());
        redisService.set(cacheKeyById, roleDto, cacheExpireSeconds);
        
        // Cache theo name (nếu có)
        if (roleDto.getName() != null && !roleDto.getName().trim().isEmpty()) {
            String cacheKeyByName = buildCacheKeyByName(roleDto.getName());
            redisService.set(cacheKeyByName, roleDto, cacheExpireSeconds);
        }
    }
    
    // ========== 🔍 GET FROM CACHE ==========
    
    /**
     * 🔍 Lấy role từ cache theo ID
     * 
     * @param roleId ID của role
     * @return RoleDto nếu tìm thấy trong cache, null nếu không có
     */
    public RoleDto getRoleFromCacheById(Integer roleId) {
        if (roleId == null) return null;
        return redisService.get(buildCacheKeyById(roleId), RoleDto.class);
    }
    
    /**
     * 🔍 Lấy role từ cache theo tên
     * 
     * @param name Tên của role
     * @return RoleDto nếu tìm thấy trong cache, null nếu không có
     */
    public RoleDto getRoleFromCacheByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return redisService.get(buildCacheKeyByName(name), RoleDto.class);
    }
    
    // ========== 🗑️ REMOVE FROM CACHE ==========
    
    /**
     * 🗑️ Xóa role khỏi Redis cache (tất cả keys liên quan)
     * 
     * Khi role bị xóa hoặc update, cần invalidate cache để đảm bảo data consistency
     * 
     * @param roleId ID của role (required)
     * @param name Tên của role (optional, null nếu không có)
     */
    public void removeRoleFromCache(Integer roleId, String name) {
        // Xóa cache theo ID
        if (roleId != null) {
            redisService.delete(buildCacheKeyById(roleId));
        }
        
        // Xóa cache theo name (nếu có)
        if (name != null && !name.trim().isEmpty()) {
            redisService.delete(buildCacheKeyByName(name));
        }
        
        // Lưu ý: Không xóa cache phân trang vì có thể có nhiều trang khác nhau
        // Nên để tự động expire hoặc invalidate khi cần (có thể xóa tất cả keys "role:page:*" nếu cần)
    }
    
    /**
     * 🗑️ Xóa tất cả cache phân trang roles (khi có thay đổi lớn)
     * 
     * Lưu ý: Có thể xóa tất cả keys "role:page:*" nếu cần, nhưng tốn performance
     * Nên để tự động expire hoặc invalidate từng cái khi update/delete
     */
    public void invalidateRolePageCache() {
        // Có thể implement xóa tất cả keys "role:page:*" nếu cần
        // Nhưng để tránh tốn performance, chỉ invalidate khi cần thiết
        // Hoặc để tự động expire
    }
    
    // ========== 🎁 CACHE-ASIDE PATTERN ==========
    
    /**
     * 🎁 Pattern chung: Lấy role từ cache, nếu không có thì lấy từ DB và cache lại
     * 
     * Cache-aside pattern (Lazy Loading):
     * 1. Check cache → Nếu có → Return ngay (FAST ⚡)
     * 2. Nếu không có → Query database
     * 3. Convert Role → RoleDto
     * 4. Lưu kết quả vào cache → Return (chậm hơn lần đầu, nhưng lần sau sẽ nhanh)
     * 
     * @param cacheValue RoleDto từ cache (null nếu không có)
     * @param dbQuery Lambda function để query từ database
     * @return Optional<RoleDto>
     */
    public Optional<RoleDto> getRoleWithCacheStrategy(
            RoleDto cacheValue,
            Supplier<Optional<Role>> dbQuery) {
        
        // ✅ Bước 1: Nếu có trong cache → Return ngay (CACHE HIT - nhanh nhất)
        if (cacheValue != null) {
            return Optional.of(cacheValue);
        }
        
        // ❌ Bước 2: Không có trong cache → Query từ database (CACHE MISS)
        Optional<Role> roleFromDb = dbQuery.get();
        
        // Bước 3: Convert Role → RoleDto
        Optional<RoleDto> roleDto = roleFromDb.map(RoleUtils::toDto);
        
        // Bước 4: Lưu vào cache để lần sau nhanh hơn
        roleDto.ifPresent(this::saveRoleToCache);
        
        return roleDto;
    }
}

