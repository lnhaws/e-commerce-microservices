package com.rainbowforest.productcatalogservice.feignclient;

import com.rainbowforest.productcatalogservice.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Tên này phải khớp với spring.application.name của Category Service
@FeignClient(name = "category-service")
public interface CategoryClient {

    @GetMapping("/categories/{id}")
    CategoryDTO getCategoryById(@PathVariable("id") Long id);
}