package com.rainbowforest.categoryservice.repository;

import com.rainbowforest.categoryservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Hàm này giúp Backend kiểm tra xem tên danh mục đã tồn tại chưa để tránh trùng lặp
    boolean existsByCategoryName(String categoryName);
}