package com.example.phonehub.service;

import com.example.phonehub.dto.CreateOrderRequest;
import com.example.phonehub.dto.OrderDto;
import com.example.phonehub.entity.*;
import com.example.phonehub.repository.*;
import com.example.phonehub.service.helper.OrderHelper;
import com.example.phonehub.utils.OrderUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderHelper orderHelper;

    public Page<OrderDto> getOrders(int page, int size) {
        Pageable p = PageRequest.of(page, size);
        return OrderUtils.toDtoPage(orderRepository.findAll(p));
    }

    public Page<OrderDto> getOrdersByUser(Integer userId, int page, int size) {
        Pageable p = PageRequest.of(page, size);
        return OrderUtils.toDtoPage(orderRepository.findByUser_Id(userId, p));
    }

    public Optional<OrderDto> getById(Integer id) {
        return orderRepository.findById(id).map(OrderUtils::toDto);
    }

    public OrderDto createOrder(CreateOrderRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(req.getPaymentMethod());
        order.setStatus("success");
        order.setNote(req.getNote());
        order.setTotalPrice(BigDecimal.ZERO);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            BigDecimal unitPrice = orderHelper.getUnitPrice(product).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountPercent = orderHelper.parseDiscountPercentage(product.getDiscount());
            BigDecimal subtotal = orderHelper.calcSubtotal(unitPrice, itemReq.getQuantity(), discountPercent);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setDiscount(discountPercent);
            item.setSubtotal(subtotal);
            items.add(item);

            total = total.add(subtotal);
        }

        order.setItems(items);
        order.setTotalPrice(total.setScale(2, RoundingMode.HALF_UP));

        Order saved = orderRepository.save(order);
        return OrderUtils.toDto(saved);
    }
}


