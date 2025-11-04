package com.example.phonehub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service tiện ích để thao tác với Redis
 * 
 * Cung cấp các method đơn giản để:
 * - Lưu/đọc dữ liệu
 * - Set thời gian hết hạn (TTL)
 * - Xóa dữ liệu
 * - Kiểm tra key tồn tại
 * 
 * Sử dụng RedisTemplate được cấu hình trong RedisConfig
 */
@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Lưu giá trị vào Redis (không có thời gian hết hạn)
     * 
     * @param key   Key để lưu (ví dụ: "user:123", "session:abc")
     * @param value Giá trị cần lưu (có thể là String, Object, List, Map...)
     * 
     * Ví dụ:
     * redisService.set("user:123", userObject);
     * redisService.set("token:abc", "refresh_token_value");
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.warn("Redis set failed for key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * Lưu giá trị vào Redis với thời gian hết hạn (TTL - Time To Live)
     * 
     * @param key      Key để lưu
     * @param value    Giá trị cần lưu
     * @param timeout  Thời gian hết hạn (seconds)
     * 
     * Ví dụ:
     * redisService.set("otp:123456", "123456", 300); // Hết hạn sau 5 phút
     */
    public void set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Redis set with timeout failed for key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * Lưu giá trị với Duration (Java 8+)
     * 
     * @param key      Key để lưu
     * @param value    Giá trị cần lưu
     * @param duration Thời gian hết hạn (Duration object)
     * 
     * Ví dụ:
     * redisService.set("session:abc", sessionData, Duration.ofMinutes(30));
     */
    public void set(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception e) {
            logger.warn("Redis set with duration failed for key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * Lấy giá trị từ Redis
     * 
     * @param key Key cần lấy
     * @return Giá trị tương ứng với key, hoặc null nếu không tồn tại
     * 
     * Ví dụ:
     * User user = (User) redisService.get("user:123");
     * String token = (String) redisService.get("token:abc");
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("Redis get failed for key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Lấy giá trị và cast về type cụ thể
     * 
     * @param key   Key cần lấy
     * @param clazz Class type của object
     * @return Object đã cast về type, hoặc null nếu không tồn tại
     * 
     * Ví dụ:
     * User user = redisService.get("user:123", User.class);
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            logger.warn("Redis get failed for key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Xóa key khỏi Redis
     * 
     * @param key Key cần xóa
     * @return true nếu xóa thành công, false nếu key không tồn tại
     * 
     * Ví dụ:
     * redisService.delete("user:123");
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            logger.warn("Redis delete failed for key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Xóa nhiều key cùng lúc
     * 
     * @param keys Set các key cần xóa
     * @return Số lượng key đã xóa thành công
     * 
     * Ví dụ:
     * Set<String> keys = Set.of("user:1", "user:2", "user:3");
     * long deleted = redisService.delete(keys);
     */
    public long delete(Set<String> keys) {
        try {
            Long deleted = redisTemplate.delete(keys);
            return deleted != null ? deleted : 0;
        } catch (Exception e) {
            logger.warn("Redis delete multiple keys failed, error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Kiểm tra key có tồn tại trong Redis không
     * 
     * @param key Key cần kiểm tra
     * @return true nếu key tồn tại, false nếu không
     * 
     * Ví dụ:
     * if (redisService.exists("user:123")) {
     *     // Key tồn tại
     * }
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.warn("Redis exists check failed for key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Set thời gian hết hạn cho key (nếu key đã tồn tại)
     * 
     * @param key     Key cần set TTL
     * @param timeout Thời gian hết hạn (seconds)
     * @return true nếu set thành công, false nếu key không tồn tại
     * 
     * Ví dụ:
     * redisService.expire("user:123", 3600); // Hết hạn sau 1 giờ
     */
    public boolean expire(String key, long timeout) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
        } catch (Exception e) {
            logger.warn("Redis expire failed for key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Set thời gian hết hạn với Duration
     * 
     * @param key      Key cần set TTL
     * @param duration Thời gian hết hạn (Duration object)
     * @return true nếu set thành công
     */
    public boolean expire(String key, Duration duration) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, duration));
        } catch (Exception e) {
            logger.warn("Redis expire with duration failed for key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thời gian còn lại của key (TTL - Time To Live)
     * 
     * @param key Key cần kiểm tra
     * @return Số giây còn lại, -1 nếu key không có TTL, -2 nếu key không tồn tại
     * 
     * Ví dụ:
     * long ttl = redisService.getTTL("user:123");
     * if (ttl > 0) {
     *     System.out.println("Key còn sống " + ttl + " giây");
     * }
     */
    public long getTTL(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            logger.warn("Redis getTTL failed for key: {}, error: {}", key, e.getMessage());
            return -2;
        }
    }

    /**
     * Tìm tất cả keys theo pattern
     * 
     * @param pattern Pattern để tìm (ví dụ: "user:*", "session:*")
     * @return Set các key khớp với pattern
     * 
     * Ví dụ:
     * Set<String> userKeys = redisService.keys("user:*");
     * // Trả về: ["user:1", "user:2", "user:3"]
     * 
     * Lưu ý: Hàm này có thể chậm với database lớn, nên dùng cẩn thận
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            logger.warn("Redis keys failed for pattern: {}, error: {}", pattern, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Tăng giá trị số (increment)
     * Nếu key chưa tồn tại, tạo mới với giá trị 0 rồi tăng lên 1
     * 
     * @param key Key cần tăng giá trị
     * @return Giá trị sau khi tăng
     * 
     * Ví dụ:
     * long count = redisService.increment("visitor:count");
     * // Lần đầu: 1, lần sau: 2, 3, 4...
     */
    public long increment(String key) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            return value != null ? value : 0;
        } catch (Exception e) {
            logger.warn("Redis increment failed for key: {}, error: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Tăng giá trị số với số lượng cụ thể
     * 
     * @param key   Key cần tăng giá trị
     * @param delta Số lượng tăng thêm
     * @return Giá trị sau khi tăng
     * 
     * Ví dụ:
     * long count = redisService.increment("visitor:count", 5);
     * // Tăng thêm 5
     */
    public long increment(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().increment(key, delta);
            return value != null ? value : 0;
        } catch (Exception e) {
            logger.warn("Redis increment with delta failed for key: {}, error: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Giảm giá trị số (decrement)
     * 
     * @param key Key cần giảm giá trị
     * @return Giá trị sau khi giảm
     */
    public long decrement(String key) {
        try {
            Long value = redisTemplate.opsForValue().decrement(key);
            return value != null ? value : 0;
        } catch (Exception e) {
            logger.warn("Redis decrement failed for key: {}, error: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Giảm giá trị số với số lượng cụ thể
     * 
     * @param key   Key cần giảm giá trị
     * @param delta Số lượng giảm đi
     * @return Giá trị sau khi giảm
     */
    public long decrement(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().decrement(key, delta);
            return value != null ? value : 0;
        } catch (Exception e) {
            logger.warn("Redis decrement with delta failed for key: {}, error: {}", key, e.getMessage());
            return 0;
        }
    }
}


