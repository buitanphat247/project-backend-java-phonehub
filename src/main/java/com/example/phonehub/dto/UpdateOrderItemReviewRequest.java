package com.example.phonehub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemReviewRequest {

    @NotNull(message = "reviewed flag is required")
    private Boolean reviewed;

    private Integer reviewId;
}
