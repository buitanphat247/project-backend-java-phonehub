package com.example.phonehub.controller;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.dto.ApiResponse;
import com.example.phonehub.dto.UploadResponse;
import com.example.phonehub.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "File Upload", description = "API upload file với Uploadcare")
@Public
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Operation(summary = "📤 Upload file từ máy tính", description = "Upload file từ máy tính lên Uploadcare")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Upload thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ File không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "❌ Lỗi upload")
    })
    @PostMapping("/file")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFile(
            @Parameter(description = "File cần upload", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            UploadResponse response = uploadService.uploadFile(file);
            ApiResponse<UploadResponse> apiResponse = ApiResponse.success("Upload file thành công", response);
            return ResponseEntity.ok(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<UploadResponse> apiResponse = ApiResponse.badRequest(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (IOException e) {
            ApiResponse<UploadResponse> apiResponse = ApiResponse.error("Lỗi khi upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        } catch (RuntimeException e) {
            ApiResponse<UploadResponse> apiResponse = ApiResponse.error("Lỗi khi upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @Operation(summary = "🗑️ Xóa file", description = "Xóa file khỏi Uploadcare")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy file")
    })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "ID của file", required = true, example = "12345678-1234-1234-1234-123456789abc")
            @PathVariable String fileId) {
        try {
            System.out.println("Attempting to delete file: " + fileId);
            boolean deleted = uploadService.deleteFile(fileId);
            System.out.println("Delete result: " + deleted);
            
            if (deleted) {
                ApiResponse<Void> apiResponse = ApiResponse.success("Xóa file thành công", null);
                return ResponseEntity.ok(apiResponse);
            } else {
                ApiResponse<Void> apiResponse = ApiResponse.notFound("Không thể xóa file hoặc file không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }
        } catch (Exception e) {
            System.out.println("Delete exception: " + e.getMessage());
            ApiResponse<Void> apiResponse = ApiResponse.error("Lỗi khi xóa file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
