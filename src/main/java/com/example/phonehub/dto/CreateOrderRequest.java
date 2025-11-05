package com.example.phonehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Integer userId;

    @NotBlank
    @Size(max = 100)
    private String buyerName;

    @Size(max = 100)
    private String buyerEmail;

    @Size(max = 20)
    private String buyerPhone;

    @Size(max = 255)
    private String buyerAddress;

    @Size(max = 50)
    private String paymentMethod = "VNPAY";

    // Tổng tiền đơn (VND). Cho phép client truyền trực tiếp để tạo order không cần items
    private BigDecimal amount;
}


