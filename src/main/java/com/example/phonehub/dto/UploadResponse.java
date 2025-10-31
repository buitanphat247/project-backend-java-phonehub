package com.example.phonehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String cdnUrl;
    private Long fileSize;
    private String mimeType;
    private String originalName;
    private boolean image;
    private String thumbnailUrl;
}
