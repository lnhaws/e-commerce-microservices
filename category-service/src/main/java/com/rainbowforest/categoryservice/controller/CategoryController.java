package com.rainbowforest.categoryservice.controller;

import com.rainbowforest.categoryservice.entity.Category;
import com.rainbowforest.categoryservice.http.header.HeaderGenerator;
import com.rainbowforest.categoryservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (!categories.isEmpty()) {
            return new ResponseEntity<>(categories, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            return new ResponseEntity<>(category, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(
            @Valid @RequestBody Category category,
            HttpServletRequest request) {
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
    public ResponseEntity<?> updateCategory(
            @PathVariable("id") Long id, 
            @Valid @RequestBody Category categoryDetails,
            HttpServletRequest request) {
        
        Category existingCategory = categoryService.getCategoryById(id);
        if (existingCategory != null) {
            try {
                existingCategory.setCategoryName(categoryDetails.getCategoryName());
                existingCategory.setDescription(categoryDetails.getDescription());
                
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            try {
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