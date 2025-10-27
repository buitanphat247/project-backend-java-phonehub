package com.example.phonehub.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
    
    /**
     * Mã hóa password theo MD5
     * @param password Password gốc
     * @return Password đã mã hóa MD5
     */
    public static String encodeMD5(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    /**
     * Kiểm tra password có khớp với hash không
     * @param password Password gốc
     * @param hash Hash đã lưu trong database
     * @return true nếu khớp, false nếu không khớp
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        String encodedPassword = encodeMD5(password);
        return encodedPassword.equals(hash);
    }
}
