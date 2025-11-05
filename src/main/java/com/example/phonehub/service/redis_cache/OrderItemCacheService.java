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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Pageable p = PageRequest.of(page, size);
        Page<OrderItem> items = orderItemRepository.findAll(p);
        List<OrderItemDto> filtered = items.getContent().stream()
                .filter(i -> i.getOrder() != null && i.getOrder().getId().equals(orderId))
                .map(OrderUtils::toItemDto)
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, p, filtered.size());
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
