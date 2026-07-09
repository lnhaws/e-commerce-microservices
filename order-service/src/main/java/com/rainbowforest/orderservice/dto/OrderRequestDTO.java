package com.rainbowforest.orderservice.dto;

// 🌟 THÊM 3 DÒNG IMPORT NÀY
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

public class OrderRequestDTO {
    
    @NotEmpty(message = "Danh sách sản phẩm thanh toán không được để trống")
    private List<Long> selectedProductIds;
    
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String address;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    
    private String notes;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

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