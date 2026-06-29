package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
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

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Nhúng Feign Client vào để gọi điện hỏi thông tin
    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 🌟 HÀM MỚI: Lấy Entity Order từ DB lên, sau đó ghép nối dữ liệu thành DTO
    public OrderResponseDTO getOrderDetails(Long orderId) {
        Order order = getOrderById(orderId);
        if (order == null)
            return null;
        return mapToOrderResponseDTO(order);
    }

    // 🌟 HÀM MỚI: Mapping toàn bộ danh sách Order thành DTO
    public List<OrderResponseDTO> getAllOrderDetails() {
        List<Order> orders = getAllOrders();
        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        // Map toàn bộ danh sách đơn hàng sang dạng DTO có đầy đủ thông tin trà/cafe từ
        // Catalog Service
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    // --- LOGIC GHÉP NỐI NẰM Ở ĐÂY ---
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

                try {
                    itemDTO.setProduct(productClient.getProductById(item.getProductId()));
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