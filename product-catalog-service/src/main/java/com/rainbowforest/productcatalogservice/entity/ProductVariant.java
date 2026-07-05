package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(name = "weight")
    @NotNull
    private Integer weight; // Ví dụ: 250, 500, 1

    @Column(name = "unit", length = 10)
    @NotNull
    private String unit; // Ví dụ: "g", "kg"

    @Column(name = "price")
    @NotNull
    private BigDecimal price; // Giá riêng cho khối lượng này

    @Column(name = "availability")
    @NotNull
    private int availability; // Số lượng tồn kho riêng của khối lượng này

    @Column(name = "image_url")
    private String imageUrl; // Ảnh tùy chọn, nếu NULL thì Frontend sẽ tự lấy ảnh gốc của Product

    public ProductVariant() {
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getAvailability() { return availability; }
    public void setAvailability(int availability) { this.availability = availability; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}