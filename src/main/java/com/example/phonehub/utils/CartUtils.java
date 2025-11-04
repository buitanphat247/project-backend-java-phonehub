package com.example.phonehub.utils;

import com.example.phonehub.dto.CartItemDto;
import com.example.phonehub.dto.ProductDto;
import com.example.phonehub.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartUtils {
    public static CartItemDto toDto(CartItem item) {
        if (item == null) return null;
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            ProductDto pd = ProductUtils.toDtoSummary(item.getProduct());
            dto.setProduct(pd);
        }
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtAdd(item.getPriceAtAdd());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static List<CartItemDto> toDtoList(List<CartItem> items) {
        return items.stream().map(CartUtils::toDto).collect(Collectors.toList());
    }
}


