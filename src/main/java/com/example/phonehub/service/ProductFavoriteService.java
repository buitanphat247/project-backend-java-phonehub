package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductFavoriteRequest;
import com.example.phonehub.dto.ProductFavoriteDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductFavorite;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductFavoriteRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.ProductUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductFavoriteService {
    @Autowired private ProductFavoriteRepository favoriteRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public ProductFavoriteDto addFavorite(Integer userId, CreateProductFavoriteRequest req){
        if (favoriteRepository.existsByUserIdAndProductId(userId, req.getProductId()))
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: "+userId));
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        ProductFavorite pf = new ProductFavorite(); pf.setUser(user); pf.setProduct(product);
        return ProductUtils.toDto(favoriteRepository.save(pf));
    }

    public void removeFavorite(Integer userId, Integer productId){
        ProductFavorite pf = favoriteRepository.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong danh sách yêu thích"));
        favoriteRepository.delete(pf);
    }

    public Page<ProductFavoriteDto> getUserFavorites(Integer userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return ProductUtils.toFavoriteDtoPage(favoriteRepository.findByUserId(userId, pageable));
    }

    public boolean isFavorite(Integer userId, Integer productId){
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    public long getFavoriteCount(Integer productId){
        return favoriteRepository.countByProductId(productId);
    }
}

