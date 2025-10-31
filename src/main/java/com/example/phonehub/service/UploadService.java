package com.example.phonehub.service;

import com.example.phonehub.config.UploadcareConfig;
import com.example.phonehub.dto.UploadResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    @Autowired
    private UploadcareConfig uploadcareConfig;

    private RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UploadService() {
        // Simple timeout configuration
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(30000);   // 30 seconds
        this.restTemplate.setRequestFactory(factory);
    }

    public UploadResponse uploadFile(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Upload file to Uploadcare
        String uploadUrl = "https://upload.uploadcare.com/base/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("UPLOADCARE_PUB_KEY", uploadcareConfig.getPublicKey());
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String fileId = jsonResponse.get("file").asText();

            // Build response
            UploadResponse uploadResponse = new UploadResponse();
            uploadResponse.setFileId(fileId);
            uploadResponse.setFileName(file.getOriginalFilename());
            uploadResponse.setFileUrl(uploadcareConfig.getCdnBase() + fileId + "/" + file.getOriginalFilename());
            uploadResponse.setCdnUrl(uploadcareConfig.getCdnBase() + fileId + "/" + file.getOriginalFilename());
            uploadResponse.setFileSize(file.getSize());
            uploadResponse.setMimeType(file.getContentType());
            uploadResponse.setOriginalName(file.getOriginalFilename());
            uploadResponse.setImage(isImageFile(file.getContentType()));
            
            // Generate thumbnail URL for images
            if (uploadResponse.isImage()) {
                uploadResponse.setThumbnailUrl(uploadcareConfig.getCdnBase() + fileId + "/-/preview/300x300/");
            }

            return uploadResponse;
        } else {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }
    }

    public UploadResponse uploadFromUrl(String fileUrl) {
        try {
            String uploadUrl = "https://upload.uploadcare.com/from_url/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("pub_key", uploadcareConfig.getPublicKey());
            body.add("source_url", fileUrl);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    throw new RuntimeException("Empty response from Uploadcare");
                }
                
                // Clean response body from control characters
                responseBody = responseBody.replaceAll("[\\x00-\\x1F\\x7F]", "");
                
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String fileId = jsonResponse.get("file_id").asText();

                // Build response
                UploadResponse uploadResponse = new UploadResponse();
                uploadResponse.setFileId(fileId);
                uploadResponse.setFileName("uploaded_file");
                uploadResponse.setFileUrl(uploadcareConfig.getCdnBase() + fileId + "/uploaded_file");
                uploadResponse.setCdnUrl(uploadcareConfig.getCdnBase() + fileId + "/uploaded_file");
                uploadResponse.setFileSize(0L);
                uploadResponse.setMimeType("application/octet-stream");
                uploadResponse.setOriginalName("uploaded_file");
                uploadResponse.setImage(false);

                return uploadResponse;
            } else {
                throw new RuntimeException("Upload from URL failed: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error uploading from URL: " + e.getMessage());
        }
    }

    public boolean deleteFile(String fileId) {
        try {
            String deleteUrl = "https://api.uploadcare.com/files/" + fileId + "/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Uploadcare.Simple " + uploadcareConfig.getPublicKey() + ":" + uploadcareConfig.getSecretKey());
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            
            System.out.println("Delete response status: " + response.getStatusCode());
            System.out.println("Delete response body: " + response.getBody());
            
            // Uploadcare có thể trả về 204 (NO_CONTENT) hoặc 200 (OK) khi xóa thành công
            return response.getStatusCode() == HttpStatus.NO_CONTENT || 
                   response.getStatusCode() == HttpStatus.OK ||
                   response.getStatusCode() == HttpStatus.ACCEPTED;
        } catch (Exception e) {
            System.out.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    public UploadResponse getFileInfo(String fileId) {
        try {
            String infoUrl = "https://api.uploadcare.com/files/" + fileId + "/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Uploadcare.Simple " + uploadcareConfig.getPublicKey() + ":" + uploadcareConfig.getSecretKey());
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(infoUrl, HttpMethod.GET, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    throw new RuntimeException("Empty response from Uploadcare");
                }
                
                // Clean response body from control characters
                responseBody = responseBody.replaceAll("[\\x00-\\x1F\\x7F]", "");
                
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                
                String originalFilename = jsonResponse.get("original_filename").asText();
                
                UploadResponse uploadResponse = new UploadResponse();
                uploadResponse.setFileId(fileId);
                uploadResponse.setFileName(originalFilename);
                uploadResponse.setFileUrl(uploadcareConfig.getCdnBase() + fileId + "/" + originalFilename);
                uploadResponse.setCdnUrl(uploadcareConfig.getCdnBase() + fileId + "/" + originalFilename);
                uploadResponse.setFileSize(jsonResponse.get("size").asLong());
                uploadResponse.setMimeType(jsonResponse.get("mime_type").asText());
                uploadResponse.setOriginalName(originalFilename);
                uploadResponse.setImage(isImageFile(jsonResponse.get("mime_type").asText()));
                
                // Generate thumbnail URL for images
                if (uploadResponse.isImage()) {
                    uploadResponse.setThumbnailUrl(uploadcareConfig.getCdnBase() + fileId + "/-/preview/300x300/");
                }

                return uploadResponse;
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy thông tin file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > uploadcareConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa: " + (uploadcareConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExts = Arrays.asList(uploadcareConfig.getAllowedExtensions().split(","));
        
        if (!allowedExts.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + uploadcareConfig.getAllowedExtensions());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }
}
