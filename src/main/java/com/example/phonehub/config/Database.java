package com.example.phonehub.config;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.phonehub.auth.annotation.Public;

@RestController
@Public
@RequestMapping("/api/v1/database")
public class Database {
    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public Map<String, Object> checkConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "OK");
            response.put("message", "PhoneHub application and database connection are working");

            Map<String, Object> dbInfo = new HashMap<>();
            dbInfo.put("status", "Connected");
            dbInfo.put("driver", connection.getMetaData().getDriverName());
            dbInfo.put("driverVersion", connection.getMetaData().getDriverVersion());
            dbInfo.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            dbInfo.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
            dbInfo.put("url", connection.getMetaData().getURL());
            dbInfo.put("username", connection.getMetaData().getUserName());
            dbInfo.put("autocommit", connection.getAutoCommit());
            dbInfo.put("isolation", connection.getTransactionIsolation());
            dbInfo.put("readOnly", connection.isReadOnly());

            response.put("database", dbInfo);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Database connection failed");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }

        return response;
    }

    @PostMapping("/migrate/remove-product-color-column")
    public Map<String, Object> removeProductColorColumn() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Try with IF EXISTS first (MySQL 8.0.19+)
            String sql = "ALTER TABLE products DROP COLUMN IF EXISTS color";
            statement.execute(sql);
            
            response.put("status", "OK");
            response.put("message", "Successfully removed 'color' column from products table");
            
        } catch (Exception e) {
            // If IF EXISTS is not supported, try without it
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "ALTER TABLE products DROP COLUMN color";
                statement.execute(sql);
                response.put("status", "OK");
                response.put("message", "Successfully removed 'color' column from products table");
            } catch (Exception e2) {
                response.put("status", "ERROR");
                response.put("message", "Failed to remove 'color' column from products table");
                response.put("error", e2.getMessage());
                response.put("errorType", e2.getClass().getSimpleName());
            }
        }

        return response;
    }

}
