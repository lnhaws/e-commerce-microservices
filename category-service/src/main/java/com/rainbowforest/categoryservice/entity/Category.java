package com.rainbowforest.categoryservice.entity;

import org.hibernate.annotations.Nationalized;
import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "category_name", nullable = false, unique = true, columnDefinition = "nvarchar(255)")
    private String categoryName;

    @Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    // Dùng Integer (đối tượng) để bắt được giá trị null, hỗ trợ tính năng Xóa mềm / Khóa danh mục
    @Column(name = "active")
    private Integer active;

    // Constructor rỗng (Bắt buộc cho JPA)
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