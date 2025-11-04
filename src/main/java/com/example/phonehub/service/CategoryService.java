package com.example.phonehub.service;

import com.example.phonehub.dto.CreateCategoryRequest;
import com.example.phonehub.dto.CategoryDto;
import com.example.phonehub.entity.Category;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.CategoryRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.redis_cache.CategoryCacheService;
import com.example.phonehub.utils.CategoryUtils;
import com.example.phonehub.utils.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryCacheService categoryCacheService;

    public Page<CategoryDto> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return CategoryUtils.toDtoPage(categoryPage);
    }

    public Optional<CategoryDto> getCategoryById(Integer id) {
        if (id == null) return Optional.empty();
        
        CategoryDto cachedCategory = categoryCacheService.getCategoryFromCacheById(id);
        return categoryCacheService.getCategoryWithCacheStrategy(
            cachedCategory,
            () -> categoryRepository.findById(id)
        );
    }
    
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) return Optional.empty();
        
        CategoryDto cachedCategory = categoryCacheService.getCategoryFromCacheBySlug(slug);
        return categoryCacheService.getCategoryWithCacheStrategy(
            cachedCategory,
            () -> categoryRepository.findBySlug(slug)
        );
    }

    public CategoryDto createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        String slug = (request.getSlug() == null || request.getSlug().isEmpty()) 
                ? SlugUtils.generateSlug(request.getName()) 
                : request.getSlug();

        if (categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }

        User admin = userRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setCreatedBy(admin);
        
        Category savedCategory = categoryRepository.save(category);
        CategoryDto savedDto = CategoryUtils.toDto(savedCategory);
        categoryCacheService.saveCategoryToCache(savedDto);
        
        return savedDto;
    }

    public CategoryDto updateCategory(Integer id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        String oldSlug = category.getSlug();

        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        
        String slug = (request.getSlug() == null || request.getSlug().isEmpty()) 
                ? SlugUtils.generateSlug(request.getName()) 
                : request.getSlug();
        
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }
        
        category.setSlug(slug);
        
        Category updatedCategory = categoryRepository.save(category);
        CategoryDto updatedDto = CategoryUtils.toDto(updatedCategory);
        
        categoryCacheService.removeCategoryFromCache(id, oldSlug);
        categoryCacheService.saveCategoryToCache(updatedDto);
        
        return updatedDto;
    }

    public void deleteCategory(Integer id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        Category category = categoryOpt.get();
        String slug = category.getSlug();
        
        categoryRepository.deleteById(id);
        categoryCacheService.removeCategoryFromCache(id, slug);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
}
