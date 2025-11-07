package com.example.phonehub.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.phonehub.config.VNPayConfig;
import com.example.phonehub.utils.VNPayHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class VNPayService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendBaseUrl;

    public String createOrder(int total, String orderInfor, String urlReturn, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        
        // Lấy IP thực từ request (quan trọng cho VPS)
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        if (vnp_IpAddr == null || vnp_IpAddr.isEmpty() || vnp_IpAddr.startsWith("Invalid")) {
            // Fallback: lấy từ request.getRemoteAddr()
            vnp_IpAddr = request.getRemoteAddr();
            if (vnp_IpAddr == null || vnp_IpAddr.isEmpty()) {
                vnp_IpAddr = "127.0.0.1";
            }
        }
        
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        // total là VND thật → gửi cho VNPay nhân 100
        vnp_Params.put("vnp_Amount", String.valueOf(VNPayHelper.toVNPAmount(total)));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        String returnBase = frontendBaseUrl != null ? frontendBaseUrl : "https://phonehub.io.vn";
        if (returnBase.endsWith("/")) { returnBase = returnBase.substring(0, returnBase.length() - 1); }
        String finalReturnUrl = returnBase + VNPayConfig.vnp_Returnurl;
        vnp_Params.put("vnp_ReturnUrl", finalReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Set timezone Asia/Ho_Chi_Minh (GMT+7) - Quan trọng để VNPay tính đúng thời gian
        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(vietnamTimeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(vietnamTimeZone); // Set timezone cho formatter
        
        // Log để debug timezone
        System.out.println("VNPay Timezone: " + vietnamTimeZone.getID());
        System.out.println("VNPay Timezone Offset (hours): " + (vietnamTimeZone.getRawOffset() / (1000 * 60 * 60)));
        
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        System.out.println("VNPay CreateDate: " + vnp_CreateDate);

        // Tăng thời gian expire lên 30 phút để tránh timeout
        cld.add(Calendar.MINUTE, 30);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        System.out.println("VNPay ExpireDate: " + vnp_ExpireDate + " (30 minutes from CreateDate)");

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) { query.append('&'); hashData.append('&'); }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) { fields.put(fieldName, fieldValue); }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) { fields.remove("vnp_SecureHashType"); }
        if (fields.containsKey("vnp_SecureHash")) { fields.remove("vnp_SecureHash"); }
        String signValue = VNPayConfig.hashAllFields(fields);

        // Parse amount thật (VND) từ callback để dùng về sau
        try {
            String amountStr = request.getParameter("vnp_Amount");
            if (amountStr != null) {
                long vnpAmount = Long.parseLong(amountStr);
                long amountVnd = VNPayHelper.fromVNPAmount(vnpAmount);
                System.out.println("VNPay amount (VND): " + amountVnd);
            }
        } catch (Exception ignored) {}

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) { return 1; }
            else { return 0; }
        } else {
            return -1;
        }
    }
}
