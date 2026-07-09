package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.Product; // Đảm bảo có cái này
import com.rainbowforest.orderservice.dto.ItemResponseDTO;
import com.rainbowforest.orderservice.dto.OrderResponseDTO;
import com.rainbowforest.orderservice.feignclient.ProductClient;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Override
    public Order saveOrder(Order order) {
        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getItems() != null) {
            for (Item item : savedOrder.getItems()) {
                try {
                    productClient.deductInventory(item.getProductId(), item.getVariantId(), item.getQuantity());
                } catch (Exception e) {
                    System.out.println("Lỗi khi trừ kho sản phẩm ID " + item.getProductId() + ": " + e.getMessage());
                }
            }
        }
        
        return savedOrder;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderResponseDTO getOrderDetails(Long orderId) {
        Order order = getOrderById(orderId);
        if (order == null)
            return null;
        return mapToOrderResponseDTO(order);
    }

    public List<OrderResponseDTO> getAllOrderDetails() {
        List<Order> orders = getAllOrders();
        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderedDate(order.getOrderedDate());
        dto.setStatus(order.getStatus());
        dto.setTotal(order.getTotal());
        dto.setFullName(order.getFullName());
        dto.setAddress(order.getAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setNotes(order.getNotes());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setUserId(order.getUserId());
        
        try {
            dto.setUser(userClient.getUserById(order.getUserId()));
        } catch (Exception e) {
            System.out.println("Lỗi gọi User Service: " + e.getMessage());
        }

        List<ItemResponseDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null) {
            for (Item item : order.getItems()) {
                ItemResponseDTO itemDTO = new ItemResponseDTO();
                itemDTO.setId(item.getId());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setSubTotal(item.getSubTotal());
                
                itemDTO.setVariantId(item.getVariantId());

                try {
                    Product product = productClient.getProductById(item.getProductId());
                    
                    if (item.getVariantId() != null && product.getVariants() != null) {
                        for (Object vObj : product.getVariants()) {
                            try {
                                Map<String, Object> variantMap = (Map<String, Object>) vObj;
                                Long vId = Long.valueOf(variantMap.get("id").toString());
                                if (vId.equals(item.getVariantId())) {
                                    String weight = variantMap.get("weight").toString() + variantMap.get("unit").toString();
                                    product.setProductName(product.getProductName() + " - " + weight);
                                    
                                    if(variantMap.get("imageUrl") != null) {
                                         product.setImageUrl(variantMap.get("imageUrl").toString());
                                    }
                                    break;
                                }
                            } catch (Exception e) {}
                        }
                    }

                    itemDTO.setProduct(product);
                } catch (Exception e) {
                    System.out.println("Lỗi gọi Product Service: " + e.getMessage());
                }
                itemDTOs.add(itemDTO);
            }
        }
        dto.setItems(itemDTOs);

        return dto;
    }
}