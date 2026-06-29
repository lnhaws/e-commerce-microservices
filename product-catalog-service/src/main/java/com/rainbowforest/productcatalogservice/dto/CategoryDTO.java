package com.rainbowforest.productcatalogservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private Integer active;

    // Getter và Setter giữ nguyên
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }
}