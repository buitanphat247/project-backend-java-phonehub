package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductImageRequest;
import com.example.phonehub.dto.ProductImageDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductImage;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductImageRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.redis_cache.ProductImageCacheService;
import com.example.phonehub.utils.ProductUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductImageService {
    @Autowired private ProductImageRepository imageRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductImageCacheService imageCacheService;

    public List<ProductImageDto> getByProduct(Integer productId) {
        if (productId == null) return List.of();
        
        List<ProductImageDto> cached = imageCacheService.getImagesFromCache(productId);
        if (cached != null) return cached;
        
        List<ProductImageDto> images = ProductUtils.toImageList(imageRepository.findByProductId(productId));
        imageCacheService.saveImagesToCache(productId, images);
        return images;
    }

    public ProductImageDto create(CreateProductImageRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));
        User admin = userRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        ProductImage i = new ProductImage();
        i.setProduct(product);
        i.setUrl(req.getUrl());
        i.setCreatedBy(admin);
        ProductImageDto savedDto = ProductUtils.toDto(imageRepository.save(i));
        imageCacheService.invalidateProductImagesCache(req.getProductId());
        return savedDto;
    }

    public ProductImageDto update(Integer id, CreateProductImageRequest req) {
        ProductImage i = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));
        i.setProduct(product);
        i.setUrl(req.getUrl());
        ProductImageDto updatedDto = ProductUtils.toDto(imageRepository.save(i));
        imageCacheService.invalidateProductImagesCache(req.getProductId());
        return updatedDto;
    }

    public void delete(Integer id) {
        ProductImage image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        Integer productId = image.getProduct().getId();
        imageRepository.deleteById(id);
        imageCacheService.invalidateProductImagesCache(productId);
    }    
}













