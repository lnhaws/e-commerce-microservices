package com.rainbowforest.categoryservice.controller;

import com.rainbowforest.categoryservice.entity.Category;
import com.rainbowforest.categoryservice.http.header.HeaderGenerator;
import com.rainbowforest.categoryservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private HeaderGenerator headerGenerator;

    // Lấy tất cả danh mục (Cho cả Admin và Khách hàng xem)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (!categories.isEmpty()) {
            return new ResponseEntity<>(categories, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // Lấy 1 danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            return new ResponseEntity<>(category, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // Thêm danh mục mới (Dành cho Admin)
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody Category category, HttpServletRequest request) {
        try {
            Category savedCategory = categoryService.saveCategory(category);
            return new ResponseEntity<>(
                    savedCategory, 
                    headerGenerator.getHeadersForSuccessPostMethod(request, savedCategory.getId()), 
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    // Sửa danh mục (Dành cho Admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody Category categoryDetails, HttpServletRequest request) {
        Category existingCategory = categoryService.getCategoryById(id);
        if (existingCategory != null) {
            try {
                // Cho phép đổi tên và mô tả
                existingCategory.setCategoryName(categoryDetails.getCategoryName());
                existingCategory.setDescription(categoryDetails.getDescription());
                
                // Cập nhật trạng thái hiển thị (Bật/Tắt danh mục)
                if (categoryDetails.getActive() != null) {
                    existingCategory.setActive(categoryDetails.getActive());
                }

                Category updatedCategory = categoryService.saveCategory(existingCategory);
                return new ResponseEntity<>(updatedCategory, headerGenerator.getHeadersForSuccessPostMethod(request, updatedCategory.getId()), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // Xóa mềm danh mục (Dành cho Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            try {
                // Xóa mềm: Chuyển active = 0 (Ẩn danh mục) thay vì xóa hẳn để không lỗi khóa ngoại với Product
                category.setActive(0);
                categoryService.saveCategory(category);
                return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }
}