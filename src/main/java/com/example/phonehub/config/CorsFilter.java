package com.example.phonehub.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CORS Filter với HIGHEST_PRECEDENCE để đảm bảo CORS headers được set trước mọi
 * filter khác
 * Xử lý cả preflight OPTIONS requests và actual requests
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Lấy origin từ request
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        // Xác định origin - ưu tiên Origin header, nếu không có thì lấy từ Referer
        String requestOrigin = origin;
        if (requestOrigin == null || requestOrigin.isEmpty()) {
            if (referer != null && !referer.isEmpty()) {
                // Extract origin từ Referer (ví dụ:
                // http://163.61.182.56:8080/swagger-ui/index.html)
                try {
                    java.net.URL refererUrl = new java.net.URL(referer);
                    requestOrigin = refererUrl.getProtocol() + "://" + refererUrl.getAuthority();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        // Luôn set CORS headers cho mọi request
        // Cho phép tất cả origins (bao gồm Vercel, localhost, VPS IP)
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Set các CORS headers bắt buộc
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers",
                "Authorization, X-New-Access-Token, X-New-Refresh-Token, X-Token-Status, Content-Type, Content-Disposition");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Xử lý preflight OPTIONS request
        // Trả về 200 OK ngay lập tức, không cần đi tiếp trong filter chain
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Dừng lại ở đây, không tiếp tục filter chain
        }

        // Tiếp tục filter chain cho các request khác
        chain.doFilter(req, res);
    }
}
