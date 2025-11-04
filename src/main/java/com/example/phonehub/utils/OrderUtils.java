package com.example.phonehub.utils;

import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.entity.Order;
import com.example.phonehub.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

public class OrderUtils {

    public static OrderItemDto toItemDto(OrderItem item) {
        if (item == null) return null;
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setSubtotal(item.getSubtotal());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static OrderDto toDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setUsername(order.getUser() != null ? order.getUser().getUsername() : null);
        dto.setTotalPrice(order.getTotalPrice());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setStatus(order.getStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(OrderUtils::toItemDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream().map(OrderUtils::toDto).collect(Collectors.toList());
    }

    public static Page<OrderDto> toDtoPage(Page<Order> page) {
        List<OrderDto> list = toDtoList(page.getContent());
        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }
}


