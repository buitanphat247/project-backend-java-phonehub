package com.example.phonehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI phoneHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PhoneHub API Documentation")
                        .description("""
                            ## 📱 PhoneHub API - Hệ thống quản lý sản phẩm điện thoại
                            
                            ### Mô tả
                            API RESTful cho hệ thống quản lý sản phẩm điện thoại, bao gồm:
                            - 🔐 Xác thực và phân quyền người dùng (JWT)
                            - 📦 Quản lý sản phẩm (Products)
                            - 🏷️ Quản lý danh mục (Categories)
                            - 🎨 Quản lý màu sắc và hình ảnh sản phẩm
                            - ⭐ Đánh giá sản phẩm (Reviews)
                            - ❤️ Sản phẩm yêu thích (Favorites)
                            - 👥 Quản lý người dùng và vai trò
                            - 🏆 Hệ thống xếp hạng người dùng (User Ranks)
                            
                            ### Xác thực
                            API sử dụng JWT Bearer Token. Vui lòng đăng nhập để lấy token và sử dụng ở mục "Authorize" phía trên.
                            
                            ### Liên kết nhanh
                            - 🔗 Swagger UI: `/swagger-ui/index.html`
                            - 📄 API Docs (JSON): `/api-docs`
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PhoneHub Team")
                                .email("support@phonehub.com")
                                .url("https://phonehub.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("🔧 Development Server (Local)"),
                        new Server()
                                .url("http://163.61.182.56:8080")
                                .description("🖥️ VPS Production Server"),
                        new Server()
                                .url("https://api.phonehub.com")
                                .description("🚀 Production Server (Future)")
                ))
                .tags(List.of(
                        new Tag().name("Home").description("Trang chủ API"),
                        new Tag().name("Authentication").description("🔐 API xác thực và đăng nhập (JWT)"),
                        new Tag().name("User Management").description("👥 API quản lý người dùng"),
                        new Tag().name("User Rank Management").description("🏆 API quản lý xếp hạng người dùng"),
                        new Tag().name("Role Management").description("🔑 API quản lý vai trò người dùng"),
                        new Tag().name("Product Management").description("📦 API quản lý sản phẩm"),
                        new Tag().name("Category Management").description("🏷️ API quản lý danh mục"),
                        new Tag().name("Product Color Management").description("🎨 API quản lý màu sắc sản phẩm"),
                        new Tag().name("Product Image Management").description("🖼️ API quản lý hình ảnh sản phẩm"),
                        new Tag().name("Product Specification Management").description("📋 API quản lý thông số kỹ thuật"),
                        new Tag().name("Product Reviews Management").description("⭐ API quản lý đánh giá sản phẩm"),
                        new Tag().name("Product Favorites").description("❤️ API quản lý sản phẩm yêu thích"),
                        new Tag().name("File Upload").description("📤 API upload file và hình ảnh")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token nhận được từ endpoint đăng nhập. Format: Bearer {token}")
                                        .name("JWT Authentication")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
