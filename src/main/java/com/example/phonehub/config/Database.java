package com.example.phonehub.config;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

}
