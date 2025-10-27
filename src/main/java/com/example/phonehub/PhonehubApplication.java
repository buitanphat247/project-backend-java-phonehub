package com.example.phonehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@Tag(name = "Home", description = "Home API")
public class PhonehubApplication {


	public static void main(String[] args) {
		SpringApplication.run(PhonehubApplication.class, args);
	}

	@Operation(summary = "Trang chủ API")
	@GetMapping("/")
	public Map<String, Object> home() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "🚀 Welcome to PhoneHub API");
		response.put("timestamp", LocalDateTime.now());
		response.put("version", "1.0.0");
		response.put("swagger_ui", "http://localhost:8080/swagger-ui/index.html");
		response.put("api_docs", "http://localhost:8080/api-docs");
		response.put("status", "API is running successfully");
		return response;
	}

}