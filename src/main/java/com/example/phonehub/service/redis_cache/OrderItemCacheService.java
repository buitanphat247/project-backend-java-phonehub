package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.OrderItemDto;
import com.example.phonehub.entity.OrderItem;
import com.example.phonehub.repository.OrderItemRepository;
import com.example.phonehub.repository.OrderRepository;
import com.example.phonehub.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderItemCacheService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Cacheable(cacheNames = "orderItems:byOrder", key = "'o:' + #orderId + ':p:' + #page + ':s:' + #size")
    public Page<OrderItemDto> listByOrder(Integer orderId, int page, int size) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> items = orderItemRepository.findByOrder_Id(orderId, pageable);
        return items.map(OrderUtils::toItemDto);
    }

    @Cacheable(cacheNames = "orderItems:byId", key = "#id")
    public Optional<OrderItemDto> byId(Integer id) {
        return orderItemRepository.findById(id).map(OrderUtils::toItemDto);
    }

    @CacheEvict(cacheNames = { "orderItems:byOrder", "orderItems:byId" }, allEntries = true)
    public void evictAll() {
        // No-op. Annotation handles eviction.
    }
}
