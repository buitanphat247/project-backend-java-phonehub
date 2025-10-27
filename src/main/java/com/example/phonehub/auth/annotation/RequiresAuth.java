package com.example.phonehub.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu route cần authentication
 * Sử dụng annotation này trên Controller hoặc method để yêu cầu đăng nhập
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuth {
    
    /**
     * Yêu cầu role cụ thể (optional)
     * Để trống nếu chỉ cần đăng nhập (không quan trọng role)
     */
    String[] roles() default {};
}

