package com.rainbowforest.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "items")
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "quantity")
    @NotNull
    private int quantity;

    @Column(name = "subtotal")
    @NotNull
    private BigDecimal subTotal;

    @Column(name = "product_id")
    @NotNull
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @ManyToMany(mappedBy = "items")
    @JsonIgnore
    private List<Order> orders;

    public Item() {}

    // 🌟 CẬP NHẬT CONSTRUCTOR: Nhận thêm variantId
    public Item(@NotNull int quantity, long productId, Long variantId, BigDecimal subTotal) {
        this.quantity = quantity;
        this.productId = productId;
        this.variantId = variantId;
        this.subTotal = subTotal;
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    // 🌟 THÊM GETTER/SETTER CHO VARIANT
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}