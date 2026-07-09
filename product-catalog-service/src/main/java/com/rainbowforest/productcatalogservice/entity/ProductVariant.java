package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.DecimalMin;

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
    @NotNull(message = "Trọng lượng không được bỏ trống")
    @Min(value = 1, message = "Trọng lượng phải lớn hơn 0")
    private Integer weight;

    @Column(name = "unit", length = 10)
    @NotNull(message = "Đơn vị không được bỏ trống")
    private String unit;

    @Column(name = "price")
    @NotNull(message = "Giá bán không được bỏ trống")
    @DecimalMin(value = "0.0", message = "Giá bán không được nhỏ hơn 0")
    private BigDecimal price;

    @Column(name = "availability")
    @NotNull(message = "Số lượng tồn kho không được bỏ trống")
    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private int availability;

    @Column(name = "image_url")
    private String imageUrl;

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