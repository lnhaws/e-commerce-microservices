package com.rainbowforest.orderservice.service;

import java.util.List;

import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.dto.OrderResponseDTO;

public interface OrderService {
    public Order saveOrder(Order order);
    Order getOrderById(Long orderId);
    List<Order> getAllOrders();
    
    OrderResponseDTO getOrderDetails(Long orderId);
    List<OrderResponseDTO> getAllOrderDetails();
    List<OrderResponseDTO> getOrdersByUserId(Long userId);
}