package com.example.phonehub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {
    @NotBlank
    private String userId;

    @NotBlank
    @Email
    private String currentEmail;

    @NotBlank
    @Email
    private String newEmail;
}


