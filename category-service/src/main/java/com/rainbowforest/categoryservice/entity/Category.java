package com.rainbowforest.categoryservice.entity;

import org.hibernate.annotations.Nationalized;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Nationalized
    @Column(name = "category_name", nullable = false, unique = true, columnDefinition = "nvarchar(255)")
    private String categoryName;

    @Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "active")
    private Integer active;

    public Category() {}

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }
}