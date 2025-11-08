package com.example.phonehub.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor để log thời gian xử lý của mỗi API request
 * Giúp phát hiện các API chậm hoặc có vấn đề về performance
 */
@Component
public class PerformanceLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUrl = queryString != null ? uri + "?" + queryString : uri;
            int status = response.getStatus();
            
            // Log với level khác nhau dựa trên thời gian xử lý
            if (duration > 5000) {
                // Rất chậm (> 5 giây)
                logger.error("🐌 SLOW API: {} {} - Status: {} - Duration: {}ms", method, fullUrl, status, duration);
            } else if (duration > 2000) {
                // Chậm (> 2 giây)
                logger.warn("⚠️  SLOW API: {} {} - Status: {} - Duration: {}ms", method, fullUrl, status, duration);
            } else if (duration > 1000) {
                // Hơi chậm (> 1 giây)
                logger.info("⏱️  API: {} {} - Status: {} - Duration: {}ms", method, fullUrl, status, duration);
            } else {
                // Bình thường - log ở INFO để luôn hiển thị
                logger.info("✅ API: {} {} - Status: {} - Duration: {}ms", method, fullUrl, status, duration);
            }
            
            // Log lỗi nếu có exception
            if (ex != null) {
                logger.error("❌ API Error: {} {} - Exception: {}", method, fullUrl, ex.getMessage(), ex);
            }
        }
    }
}

