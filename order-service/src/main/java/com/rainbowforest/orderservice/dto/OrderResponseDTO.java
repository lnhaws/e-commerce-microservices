package com.rainbowforest.orderservice.dto;

import com.rainbowforest.orderservice.domain.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderResponseDTO {
    private Long id;
    private LocalDate orderedDate;
    private String status;
    private BigDecimal total;
    private User user;
    private Long userId;
    private List<ItemResponseDTO> items;
    
    // Thông tin giao hàng
    private String fullName;
    private String address;
    private String phoneNumber;
    private String notes;
    private String paymentMethod;

    // Getter và Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getOrderedDate() { return orderedDate; }
    public void setOrderedDate(LocalDate orderedDate) { this.orderedDate = orderedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<ItemResponseDTO> getItems() { return items; }
    public void setItems(List<ItemResponseDTO> items) { this.items = items; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}