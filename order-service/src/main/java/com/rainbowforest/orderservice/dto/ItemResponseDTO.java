package com.rainbowforest.orderservice.dto;

import com.rainbowforest.orderservice.domain.Product;
import java.math.BigDecimal;

public class ItemResponseDTO {
    private Long id;
    private int quantity;
    private BigDecimal subTotal;
    private Product product;
    
    private Long variantId;

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    // 🌟 GETTER/SETTER CHO VARIANT ID
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
}