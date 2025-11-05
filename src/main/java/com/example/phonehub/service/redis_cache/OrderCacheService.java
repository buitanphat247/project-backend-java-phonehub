package com.example.phonehub.service.redis_cache;

import com.example.phonehub.dto.OrderDto;
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
public class OrderCacheService {

    @Autowired
    private OrderRepository orderRepository;

    @Cacheable(cacheNames = "orders:list", key = "'success:p:' + #page + ':s:' + #size")
    public Page<OrderDto> list(int page, int size) {
        Pageable p = PageRequest.of(page, size);
        return OrderUtils.toDtoPage(orderRepository.findByStatus("success", p));
    }

    @Cacheable(cacheNames = "orders:listByUser", key = "'u:' + #userId + ':success:p:' + #page + ':s:' + #size")
    public Page<OrderDto> listByUser(Integer userId, int page, int size) {
        Pageable p = PageRequest.of(page, size);
        return OrderUtils.toDtoPage(orderRepository.findByUser_IdAndStatus(userId, "success", p));
    }

    @Cacheable(cacheNames = "orders:byId", key = "#id")
    public Optional<OrderDto> byId(Integer id) {
        return orderRepository.findById(id).map(OrderUtils::toDto);
    }

    @CacheEvict(cacheNames = { "orders:list", "orders:listByUser", "orders:byId", "orders:spent" }, allEntries = true)
    public void evictAll() {
        // No-op. Annotation handles eviction.
    }

    @Cacheable(cacheNames = "orders:spent", key = "'u:' + #userId")
    public java.math.BigDecimal totalSpentByUser(Integer userId) {
        return orderRepository.sumTotalSpentByUser(userId);
    }
}
