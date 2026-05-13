package com.rainbowforest.orderservice.dto;

import java.util.List;

public class OrderRequestDTO {
    // Mảng ID sản phẩm khách đã tick chọn
    private List<Long> selectedProductIds;
    
    // Thông tin giao hàng và thanh toán Frontend gửi xuống
    private String fullName;
    private String address;
    private String phoneNumber;
    private String notes;
    private String paymentMethod;

    // --- GETTER VÀ SETTER ---
    public List<Long> getSelectedProductIds() { return selectedProductIds; }
    public void setSelectedProductIds(List<Long> selectedProductIds) { this.selectedProductIds = selectedProductIds; }

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