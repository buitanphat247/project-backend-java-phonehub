package com.example.phonehub.service;

import com.example.phonehub.dto.CreateProductSpecificationRequest;
import com.example.phonehub.dto.ProductSpecificationDto;
import com.example.phonehub.entity.Product;
import com.example.phonehub.entity.ProductSpecification;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.ProductRepository;
import com.example.phonehub.repository.ProductSpecificationRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.ProductUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductSpecificationService {
    @Autowired private ProductSpecificationRepository specRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public List<ProductSpecificationDto> getByProduct(Integer productId){ return ProductUtils.toSpecList(specRepository.findByProductId(productId)); }
    public List<ProductSpecificationDto> getByProductAndGroup(Integer productId, String group){ return ProductUtils.toSpecList(specRepository.findByProductIdAndGroupName(productId, group)); }

    public ProductSpecificationDto create(CreateProductSpecificationRequest req){
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        User admin = userRepository.findById(1).orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));
        ProductSpecification s = new ProductSpecification(); s.setProduct(product); s.setGroupName(req.getGroupName()); s.setLabel(req.getLabel()); s.setValue(req.getValue()); s.setType(req.getType()); s.setCreatedBy(admin);
        return ProductUtils.toDto(specRepository.save(s));
    }

    public ProductSpecificationDto update(Integer id, CreateProductSpecificationRequest req){
        ProductSpecification s = specRepository.findById(id).orElseThrow(() -> new RuntimeException("Specification not found with id: "+id));
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: "+req.getProductId()));
        s.setProduct(product); s.setGroupName(req.getGroupName()); s.setLabel(req.getLabel()); s.setValue(req.getValue()); s.setType(req.getType());
        return ProductUtils.toDto(specRepository.save(s));
    }

    public void delete(Integer id){ if (!specRepository.existsById(id)) throw new RuntimeException("Specification not found with id: "+id); specRepository.deleteById(id);}    
}




