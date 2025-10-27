package com.example.phonehub.service;

import com.example.phonehub.dto.CreateCategoryRequest;
import com.example.phonehub.dto.CategoryDto;
import com.example.phonehub.entity.Category;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.CategoryRepository;
import com.example.phonehub.repository.UserRepository;
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

    // Lấy categories với phân trang
    public Page<CategoryDto> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return CategoryUtils.toDtoPage(categoryPage);
    }

    // Lấy category theo ID
    public Optional<CategoryDto> getCategoryById(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(CategoryUtils::toDto);
    }

    // Lấy category theo slug
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        Optional<Category> category = categoryRepository.findBySlug(slug);
        return category.map(CategoryUtils::toDto);
    }

    // Tạo category mới với user ID = 1 (admin mặc định)
    public CategoryDto createCategory(CreateCategoryRequest request) {
        // Kiểm tra name đã tồn tại chưa
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        // Tự động tạo slug từ name nếu slug không được cung cấp hoặc rỗng
        String slug = (request.getSlug() == null || request.getSlug().isEmpty()) 
                ? SlugUtils.generateSlug(request.getName()) 
                : request.getSlug();

        // Kiểm tra slug đã tồn tại chưa
        if (categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }

        // Lấy user ID = 1 (admin mặc định)
        User admin = userRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin user with ID 1 not found"));

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setCreatedBy(admin);
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryUtils.toDto(savedCategory);
    }

    // Cập nhật category
    public CategoryDto updateCategory(Integer id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Kiểm tra name mới có trùng không
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        
        // Tự động tạo slug từ name nếu slug không được cung cấp hoặc rỗng
        String slug = (request.getSlug() == null || request.getSlug().isEmpty()) 
                ? SlugUtils.generateSlug(request.getName()) 
                : request.getSlug();
        
        // Kiểm tra slug mới có trùng không
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }
        
        category.setSlug(slug);
        
        Category updatedCategory = categoryRepository.save(category);
        return CategoryUtils.toDto(updatedCategory);
    }

    // Xóa category
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // Kiểm tra category có tồn tại không
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
}
