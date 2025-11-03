package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductImageRequest;
import com.example.phonehub.dto.ProductImageDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductImage;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductImageRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
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

    public List<ProductImageDto> getByProduct(Integer productId){ return ProductUtils.toImageList(imageRepository.findByProductId(productId)); }

    public ProductImageDto create(CreateProductImageRequest req){
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        User admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        ProductImage i = new ProductImage(); i.setProduct(product); i.setUrl(req.getUrl()); i.setCreatedBy(admin);
        return ProductUtils.toDto(imageRepository.save(i));
    }

    public ProductImageDto update(Integer id, CreateProductImageRequest req){
        ProductImage i = imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found with id: "+id));
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        i.setProduct(product); i.setUrl(req.getUrl());
        return ProductUtils.toDto(imageRepository.save(i));
    }

    public void delete(Integer id){ if (!imageRepository.existsById(id)) throw new RuntimeException("Image not found with id: "+id); imageRepository.deleteById(id);}    
}











