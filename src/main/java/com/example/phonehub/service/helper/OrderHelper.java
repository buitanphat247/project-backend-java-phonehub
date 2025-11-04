package com.example.phonehub.service.helper;

import com.example.phonehub.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OrderHelper {

    public BigDecimal parseDiscountPercentage(String discountText) {
        if (discountText == null || discountText.isBlank()) return BigDecimal.ZERO;
        String cleaned = discountText.trim().replace("%", "");
        try {
            BigDecimal val = new BigDecimal(cleaned);
            if (val.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
            if (val.compareTo(new BigDecimal("100")) > 0) return new BigDecimal("100");
            return val.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getUnitPrice(Product product) {
        return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
    }

    public BigDecimal calcSubtotal(BigDecimal unitPrice, int quantity, BigDecimal discountPercent) {
        BigDecimal qty = new BigDecimal(quantity);
        BigDecimal gross = unitPrice.multiply(qty);
        BigDecimal discountAmount = gross.multiply(discountPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal subtotal = gross.subtract(discountAmount);
        return subtotal.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}


