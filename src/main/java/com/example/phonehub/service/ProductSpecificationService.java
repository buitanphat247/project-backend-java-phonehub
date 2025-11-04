package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductSpecificationRequest;
import com.example.phonehub.dto.ProductSpecificationDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductSpecification;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.ProductSpecificationRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.redis_cache.ProductSpecificationCacheService;
import com.example.phonehub.utils.ProductUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductSpecificationService {
    @Autowired
    private ProductSpecificationRepository specRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductSpecificationCacheService specCacheService;

    public List<ProductSpecificationDto> getByProduct(Integer productId) {
        if (productId == null) return List.of();
        
        List<ProductSpecificationDto> cached = specCacheService.getSpecsFromCache(productId);
        if (cached != null) return cached;
        
        List<ProductSpecificationDto> specs = ProductUtils.toSpecList(specRepository.findByProductId(productId));
        specCacheService.saveSpecsToCache(productId, specs);
        return specs;
    }

    public List<ProductSpecificationDto> getByProductAndGroup(Integer productId, String group) {
        if (productId == null || group == null) return List.of();
        
        List<ProductSpecificationDto> cached = specCacheService.getSpecsByGroupFromCache(productId, group);
        if (cached != null) return cached;
        
        List<ProductSpecificationDto> specs = ProductUtils.toSpecList(specRepository.findByProductIdAndGroupName(productId, group));
        specCacheService.saveSpecsByGroupToCache(productId, group, specs);
        return specs;
    }

    public ProductSpecificationDto create(CreateProductSpecificationRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));
        User admin = userRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        ProductSpecification s = new ProductSpecification();
        s.setProduct(product);
        s.setGroupName(req.getGroupName());
        s.setLabel(req.getLabel());
        s.setValue(req.getValue());
        s.setType(req.getType());
        s.setCreatedBy(admin);
        ProductSpecificationDto savedDto = ProductUtils.toDto(specRepository.save(s));
        specCacheService.invalidateProductSpecsCache(req.getProductId());
        return savedDto;
    }

    public ProductSpecificationDto update(Integer id, CreateProductSpecificationRequest req) {
        ProductSpecification s = specRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specification not found with id: " + id));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + req.getProductId()));
        s.setProduct(product);
        s.setGroupName(req.getGroupName());
        s.setLabel(req.getLabel());
        s.setValue(req.getValue());
        s.setType(req.getType());
        ProductSpecificationDto updatedDto = ProductUtils.toDto(specRepository.save(s));
        specCacheService.invalidateProductSpecsCache(req.getProductId());
        return updatedDto;
    }

    public void delete(Integer id) {
        ProductSpecification spec = specRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specification not found with id: " + id));
        Integer productId = spec.getProduct().getId();
        specRepository.deleteById(id);
        specCacheService.invalidateProductSpecsCache(productId);
    }
}
