package com.rainbowforest.orderservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rainbowforest.orderservice.domain.Product;

@FeignClient(name = "product-catalog-service")
public interface ProductClient {
    @GetMapping(value = "/products/{id}")
    public Product getProductById(@PathVariable(value = "id") Long productId);

    @PostMapping("/products/{id}/deduct")
    void deductInventory(
            @PathVariable("id") Long id, 
            @RequestParam(value = "variantId", required = false) Long variantId, 
            @RequestParam("quantity") int quantity
    );
}
