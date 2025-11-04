package com.example.phonehub.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình Redis cho ứng dụng PhoneHub
 * 
 * Redis được sử dụng để:
 * - Cache dữ liệu (session, user data, product data...)
 * - Lưu trữ temporary data (OTP, verification tokens...)
 * - Rate limiting
 * - Distributed locking
 */
@Configuration
public class RedisConfig {

    /**
     * Cấu hình RedisConnectionFactory
     * Lettuce là client mặc định của Spring Boot 2.x+
     * - Non-blocking, thread-safe
     * - Hỗ trợ reactive programming
     * - Tự động quản lý connection pool
     * 
     * Connection factory này sẽ tự động đọc cấu hình từ application.properties:
     * - spring.data.redis.host
     * - spring.data.redis.port
     * - spring.data.redis.password
     * - spring.data.redis.database
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Cấu hình RedisTemplate - công cụ chính để thao tác với Redis
     * 
     * RedisTemplate là wrapper trên Redis connection, cung cấp các method:
     * - set(key, value) - lưu giá trị
     * - get(key) - lấy giá trị
     * - delete(key) - xóa key
     * - expire(key, duration) - set thời gian hết hạn
     * - hasKey(key) - kiểm tra key có tồn tại
     * 
     * Serialization:
     * - Key serializer: StringRedisSerializer (key là string để dễ đọc trong Redis)
     * - Value serializer: GenericJackson2JsonRedisSerializer (convert object -> JSON)
     * 
     * Lý do dùng JSON serializer:
     * - Lưu được object phức tạp (List, Map, custom objects...)
     * - Dễ debug (có thể đọc được trong Redis CLI)
     * - Tương thích với nhiều ngôn ngữ khác
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // Set connection factory
        template.setConnectionFactory(connectionFactory);
        
        // Cấu hình serializer cho key (String)
        // Key trong Redis sẽ là string thuần, ví dụ: "user:123", "session:abc"
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Cấu hình serializer cho value (JSON)
        // Value sẽ được serialize thành JSON string, ví dụ: {"id": 123, "name": "John"}
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);
        
        // Bật transaction support (nếu cần)
        template.setEnableTransactionSupport(false);
        
        // Initialize template
        template.afterPropertiesSet();
        
        return template;
    }

}


