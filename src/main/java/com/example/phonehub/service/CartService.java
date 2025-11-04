package com.example.phonehub.service;

import com.example.phonehub.dto.CartItemDto;
import com.example.phonehub.dto.CreateCartItemRequest;
import com.example.phonehub.dto.UpdateCartItemRequest;
import com.example.phonehub.entity.CartItem;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.CartItemRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.CartUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    public List<CartItemDto> getUserCart(Integer userId) {
        List<CartItem> items = cartItemRepository.findByUser_Id(userId);
        return CartUtils.toDtoList(items);
    }

    public CartItemDto addOrUpdate(CreateCartItemRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));

        CartItem item = cartItemRepository.findByUser_IdAndProduct_Id(req.getUserId(), req.getProductId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setProduct(product);
                    // Giá tại thời điểm thêm
                    BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                    ci.setPriceAtAdd(price);
                    return ci;
                });

        int newQuantity = (item.getId() == null ? 0 : item.getQuantity()) + req.getQuantity();
        if (newQuantity <= 0) newQuantity = 1;
        item.setQuantity(newQuantity);

        CartItem saved = cartItemRepository.save(item);
        return CartUtils.toDto(saved);
    }

    public CartItemDto updateQuantity(Integer cartItemId, UpdateCartItemRequest req) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));
        item.setQuantity(req.getQuantity());
        return CartUtils.toDto(cartItemRepository.save(item));
    }

    public void removeItem(Integer cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new RuntimeException("Cart item not found: " + cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearUserCart(Integer userId) {
        cartItemRepository.deleteByUser_Id(userId);
    }
}


