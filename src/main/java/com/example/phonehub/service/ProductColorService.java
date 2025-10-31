package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductColorRequest;
import com.example.phonehub.dto.ProductColorDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductColor;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductColorRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.ProductUtils;
import com.example.phonehub.utils.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductColorService {
    @Autowired private ProductColorRepository colorRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public List<ProductColorDto> getByProduct(Integer productId){ return ProductUtils.toColorList(colorRepository.findByProductId(productId)); }
    public Optional<ProductColorDto> getBySlug(String slug){ return colorRepository.findBySlug(slug).map(ProductUtils::toDto);}    

    public ProductColorDto create(CreateProductColorRequest req){
        String slug = (req.getSlug()==null||req.getSlug().isEmpty()) ? SlugUtils.generateSlug(req.getName()) : req.getSlug();
        if (colorRepository.existsBySlug(slug)) throw new RuntimeException("Color with slug '"+slug+"' already exists");
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        User admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        ProductColor c = new ProductColor(); c.setProduct(product); c.setName(req.getName()); c.setSlug(slug); c.setHexColor(req.getHexColor()); c.setCreatedBy(admin);
        return ProductUtils.toDto(colorRepository.save(c));
    }

    public ProductColorDto update(Integer id, CreateProductColorRequest req){
        ProductColor c = colorRepository.findById(id).orElseThrow(() -> new RuntimeException("Color not found with id: "+id));
        String slug = (req.getSlug()==null||req.getSlug().isEmpty()) ? SlugUtils.generateSlug(req.getName()) : req.getSlug();
        if (!c.getSlug().equals(slug) && colorRepository.existsBySlug(slug)) throw new RuntimeException("Color with slug '"+slug+"' already exists");
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        c.setProduct(product); c.setName(req.getName()); c.setSlug(slug); c.setHexColor(req.getHexColor());
        return ProductUtils.toDto(colorRepository.save(c));
    }

    public void delete(Integer id){ if (!colorRepository.existsById(id)) throw new RuntimeException("Color not found with id: "+id); colorRepository.deleteById(id);}    
}



