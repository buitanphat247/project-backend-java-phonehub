package com.example.phonehub.service;

import com.example.phonehub.dto.CreateOrderRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.entity.*;
import com.example.phonehub.repository.*;
import com.example.phonehub.utils.OrderUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    // ProductRepository & OrderHelper không còn dùng khi tạo order theo amount

    @Autowired
    private com.example.phonehub.service.redis_cache.OrderCacheService orderCacheService;

    public Page<OrderDto> getOrders(int page, int size) {
        return orderCacheService.list(page, size);
    }

    public Page<OrderDto> getOrdersByUser(Integer userId, int page, int size) {
        return orderCacheService.listByUser(userId, page, size);
    }

    public Optional<OrderDto> getById(Integer id) {
        return orderCacheService.byId(id);
    }

    public OrderDto createOrder(CreateOrderRequest req) {

        Order order = new Order();

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
            order.setUser(user);
        }

        order.setBuyerName(req.getBuyerName());
        order.setBuyerEmail(req.getBuyerEmail());
        order.setBuyerPhone(req.getBuyerPhone());
        order.setBuyerAddress(req.getBuyerAddress());
        order.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "VNPAY");
        order.setStatus("pending");
        order.setTotalPrice((req.getAmount()));
        order.setItems(new ArrayList<>());

        Order saved = orderRepository.save(order);
        orderCacheService.evictAll();
        return OrderUtils.toDto(saved);
    }

    public OrderDto updateStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        orderCacheService.evictAll();
        return OrderUtils.toDto(saved);
    }
}
