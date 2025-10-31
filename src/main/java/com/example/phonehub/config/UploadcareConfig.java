package com.example.phonehub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadcareConfig {

    @Value("${uploadcare.public-key}")
    private String publicKey;

    @Value("${uploadcare.secret-key}")
    private String secretKey;

    @Value("${uploadcare.cdn-base}")
    private String cdnBase;

    @Value("${uploadcare.max-file-size}")
    private Long maxFileSize;

    @Value("${uploadcare.allowed-extensions}")
    private String allowedExtensions;

    public String getPublicKey() {
        return publicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getCdnBase() {
        return cdnBase;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }
}
