package com.rainbowforest.orderservice.service;

import java.util.List;

import com.rainbowforest.orderservice.domain.Order;

public interface OrderService {
    public Order saveOrder(Order order);

    Order getOrderById(Long orderId);

    List<Order> getAllOrders();
}
