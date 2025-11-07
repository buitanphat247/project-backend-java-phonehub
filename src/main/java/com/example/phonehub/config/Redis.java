package com.example.phonehub.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.service.RedisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Public
@RequestMapping("/api/v1/redis")
@Tag(name = "Redis", description = "⚡ API kiểm tra trạng thái Redis và cache")
public class Redis {
    
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    private RedisService redisService;

    @Operation(
        summary = "⚡ Kiểm tra trạng thái kết nối Redis",
        description = """
            Kiểm tra xem Redis đã được cấu hình và hoạt động đúng chưa.
            
            Endpoint này sẽ thực hiện các test:
            1. **Ping Test**: Kiểm tra kết nối cơ bản với Redis server
            2. **Set/Get Test**: Kiểm tra khả năng lưu và đọc dữ liệu từ Redis
            3. **Connection Info**: Lấy thông tin về kết nối Redis
            
            **Response Status:**
            - `OK`: Redis hoạt động bình thường
            - `WARNING`: Redis có vấn đề nhỏ
            - `ERROR`: Redis không kết nối được hoặc có lỗi nghiêm trọng
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Kiểm tra Redis thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = {
                    @ExampleObject(
                        name = "Redis hoạt động tốt",
                        value = """
                            {
                              "status": "OK",
                              "message": "Redis connection is working properly",
                              "timestamp": "2024-01-01T10:00:00",
                              "tests": {
                                "ping": {
                                  "success": true,
                                  "response": "PONG"
                                },
                                "setGet": {
                                  "success": true,
                                  "testValue": "Redis is working! 1704067200000",
                                  "retrievedValue": "Redis is working! 1704067200000"
                                }
                              },
                              "redis": {
                                "connected": true,
                                "ping": "PONG"
                              },
                              "configuration": {
                                "host": "connected"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Redis có lỗi",
                        value = """
                            {
                              "status": "ERROR",
                              "message": "Redis connection failed",
                              "timestamp": "2024-01-01T10:00:00",
                              "error": "Connection refused",
                              "errorType": "RedisConnectionException",
                              "errorClass": "org.springframework.data.redis.connection.RedisConnectionException"
                            }
                            """
                    )
                }
            )
        )
    })
    @GetMapping("/health")
    public Map<String, Object> checkConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Test 1: Ping Redis connection
            boolean pingSuccess = false;
            String pingResponse = null;
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                pingResponse = connection.ping();
                pingSuccess = "PONG".equals(pingResponse);
            }

            // Test 2: Set and Get a test value
            String testKey = "phonehub:health:check";
            String testValue = "Redis is working! " + System.currentTimeMillis();
            boolean setGetSuccess = false;
            String retrievedValue = null;
            
            try {
                redisService.set(testKey, testValue, 10); // Set với TTL 10 giây
                Object retrieved = redisService.get(testKey);
                retrievedValue = retrieved != null ? retrieved.toString() : null;
                setGetSuccess = testValue.equals(retrievedValue);
                
                // Clean up test key
                redisService.delete(testKey);
            } catch (Exception e) {
                // Error will be caught below
            }

            // Test 3: Get Redis connection info
            Map<String, Object> redisInfo = new HashMap<>();
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                redisInfo.put("connected", true);
                redisInfo.put("ping", pingResponse);
            }

            // Determine overall status
            boolean overallSuccess = pingSuccess && setGetSuccess;
            
            if (overallSuccess) {
                response.put("status", "OK");
                response.put("message", "Redis connection is working properly");
            } else {
                response.put("status", "WARNING");
                response.put("message", "Redis connection has issues");
            }

            Map<String, Object> tests = new HashMap<>();
            tests.put("ping", Map.of(
                "success", pingSuccess,
                "response", pingResponse != null ? pingResponse : "null"
            ));
            tests.put("setGet", Map.of(
                "success", setGetSuccess,
                "testValue", testValue,
                "retrievedValue", retrievedValue != null ? retrievedValue : "null"
            ));

            response.put("tests", tests);
            response.put("redis", redisInfo);
            
            // Add configuration info
            Map<String, Object> config = new HashMap<>();
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                config.put("host", connection.getNativeConnection() != null ? "connected" : "unknown");
            } catch (Exception e) {
                config.put("host", "unknown");
            }
            response.put("configuration", config);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Redis connection failed");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            response.put("errorClass", e.getClass().getName());
            
            // Add stack trace for debugging (first 5 lines)
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                String[] stackTraceLines = new String[Math.min(5, stackTrace.length)];
                for (int i = 0; i < stackTraceLines.length; i++) {
                    stackTraceLines[i] = stackTrace[i].toString();
                }
                response.put("stackTrace", stackTraceLines);
            }
        }

        return response;
    }
}

