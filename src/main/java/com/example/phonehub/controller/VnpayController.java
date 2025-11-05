package com.example.phonehub.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.phonehub.auth.annotation.Public;
import com.example.phonehub.service.VNPayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/vnpay")
@Tag(name = "VNPay", description = "API thanh toán VNPay. amount là VND thật; gửi lên VNPay = amount×100, callback chia lại /100.")
@Public
public class VnpayController {

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/submitOrder")
    @Operation(summary = "Tạo giao dịch VNPay (redirect)", description = "amount: số tiền VND (>=5,000 và <1 tỷ). Trả về chuỗi redirect:<url> cho trình duyệt.")
    @Public
    public String submidOrder(
            @Parameter(description = "Số tiền VND thật, ví dụ 37900000") @RequestParam("amount") int orderTotal,
            @Parameter(description = "Mô tả đơn hàng") @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);
        return "redirect:" + vnpayUrl;
    }

    @GetMapping("/vnpay-payment")
    @Operation(summary = "VNPay return URL", description = "VNPay redirect về FE theo app.frontend.url; endpoint này trả về view demo nếu hit trực tiếp.")
    @Public
    public String GetMapping(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }

}
