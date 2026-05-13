package com.rainbowforest.categoryservice.service;

import com.rainbowforest.categoryservice.entity.Category;
import com.rainbowforest.categoryservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category saveCategory(Category category) throws Exception {
        // Kiểm tra logic Thêm mới
        if (category.getId() == null) {
            // Check trùng tên danh mục
            if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
                throw new Exception("Tên danh mục đã tồn tại!");
            }
            // Mặc định tạo ra là được phép hoạt động (active = 1) nếu Frontend không gửi gì
            if (category.getActive() == null) {
                category.setActive(1);
            }
        }
        return categoryRepository.save(category);
    }
}