package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductRequest;
import com.example.phonehub.dto.ProductDto;
import com.example.phonehub.entity.Category;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.CategoryRepository;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.ProductUtils;
import com.example.phonehub.utils.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    public Page<ProductDto> getAll(int page, int size){ Pageable p= PageRequest.of(page,size); return ProductUtils.toDtoPageSummary(productRepository.findAll(p)); }
    public Page<ProductDto> getPublished(int page, int size){ return ProductUtils.toDtoPageSummary(productRepository.findPublishedProducts(PageRequest.of(page,size))); }
    public Page<ProductDto> getPublishedByCategory(Integer categoryId,int page,int size){ return ProductUtils.toDtoPageSummary(productRepository.findPublishedProductsByCategory(categoryId, PageRequest.of(page,size))); }
    public Page<ProductDto> getPublishedByBrand(String brand,int page,int size){ return ProductUtils.toDtoPageSummary(productRepository.findPublishedProductsByBrand(brand, PageRequest.of(page,size))); }
    public Optional<ProductDto> getById(Integer id){ return productRepository.findById(id).map(ProductUtils::toDto);}    
    public Optional<ProductDto> getBySlug(String slug){ return productRepository.findBySlug(slug).map(ProductUtils::toDto);} 

    public ProductDto create(CreateProductRequest req){
        String slug = (req.getSlug()==null || req.getSlug().isEmpty()) ? SlugUtils.generateSlug(req.getName()) : req.getSlug();
        if (productRepository.existsBySlug(slug)) throw new RuntimeException("Product with slug '"+slug+"' already exists");
        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found: "+req.getCategoryId()));
        User admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        Product p = new Product();
        p.setName(req.getName()); p.setSlug(slug); p.setBrand(req.getBrand()); p.setCategory(category);
        p.setPrice(req.getPrice()); p.setPriceOld(req.getPriceOld()); p.setDiscount(req.getDiscount()); p.setThumbnailImage(req.getThumbnailImage());
        p.setIsPublished(req.getIsPublished()!=null?req.getIsPublished():false);
        p.setPublishedAt(Boolean.TRUE.equals(p.getIsPublished()) ? LocalDateTime.now() : null);
        p.setCreatedBy(admin);
        return ProductUtils.toDto(productRepository.save(p));
    }

    public ProductDto update(Integer id, CreateProductRequest req){
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: "+id));
        String slug = (req.getSlug()==null || req.getSlug().isEmpty()) ? SlugUtils.generateSlug(req.getName()) : req.getSlug();
        if (!p.getSlug().equals(slug) && productRepository.existsBySlug(slug)) throw new RuntimeException("Product with slug '"+slug+"' already exists");
        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found: "+req.getCategoryId()));
        p.setName(req.getName()); p.setSlug(slug); p.setBrand(req.getBrand()); p.setCategory(category);
        p.setPrice(req.getPrice()); p.setPriceOld(req.getPriceOld()); p.setDiscount(req.getDiscount()); p.setThumbnailImage(req.getThumbnailImage());
        Boolean publish = req.getIsPublished();
        if (publish!=null){ p.setIsPublished(publish); p.setPublishedAt(publish? (p.getPublishedAt()==null? LocalDateTime.now(): p.getPublishedAt()): null);}        
        return ProductUtils.toDto(productRepository.save(p));
    }

    public void delete(Integer id){ if (!productRepository.existsById(id)) throw new RuntimeException("Product not found with id: "+id); productRepository.deleteById(id);}    
}



