package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Order;
import com.rainbowforest.orderservice.domain.User;
import com.rainbowforest.orderservice.feignclient.UserClient;
import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.service.CartService;
import com.rainbowforest.orderservice.service.OrderService;
import com.rainbowforest.orderservice.dto.OrderRequestDTO;
import com.rainbowforest.orderservice.dto.OrderResponseDTO;
import com.rainbowforest.orderservice.utilities.OrderUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@RestController
public class OrderController {

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private HeaderGenerator headerGenerator;

    @PostMapping(value = "/order/{userId}")
    public ResponseEntity<Order> saveOrder(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "Cart-Id") String cartId,
            @RequestBody OrderRequestDTO orderRequest,
            HttpServletRequest request) {

        List<Item> selectedItems = cartService.getSelectedItemsFromCart(cartId, orderRequest.getSelectedProductIds());
        User user = userClient.getUserById(userId);

        if (selectedItems != null && !selectedItems.isEmpty() && user != null) {
            Order order = this.createOrder(selectedItems, userId);

            order.setFullName(orderRequest.getFullName());
            order.setAddress(orderRequest.getAddress());
            order.setPhoneNumber(orderRequest.getPhoneNumber());
            order.setNotes(orderRequest.getNotes());
            order.setPaymentMethod(orderRequest.getPaymentMethod());

            try {
                orderService.saveOrder(order);

                // 🌟 FIX BUG 1: CHỈ XÓA GIỎ HÀNG NẾU LÀ THANH TOÁN TIỀN MẶT (COD)
                if ("COD".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                    cartService.deleteSelectedItemsFromCart(cartId, orderRequest.getSelectedProductIds());
                }

                return new ResponseEntity<Order>(
                        order,
                        headerGenerator.getHeadersForSuccessPostMethod(request, order.getId()),
                        HttpStatus.CREATED);
            } catch (Exception ex) {
                ex.printStackTrace();
                return new ResponseEntity<Order>(
                        headerGenerator.getHeadersForError(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Order>(
                headerGenerator.getHeadersForError(),
                HttpStatus.BAD_REQUEST);
    }

    private Order createOrder(List<Item> cart, Long userId) {
        Order order = new Order();
        order.setItems(cart);
        order.setUserId(userId);
        order.setTotal(OrderUtilities.countTotalPrice(cart));
        order.setOrderedDate(LocalDate.now());
        order.setStatus("PENDING");
        return order;
    }

    @GetMapping(value = "/order/details/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("orderId") Long orderId) {
        try {
            // Gọi hàm getOrderDetails mới viết bên Service
            OrderResponseDTO orderDto = orderService.getOrderDetails(orderId);
            if (orderDto != null) {
                return new ResponseEntity<OrderResponseDTO>(
                        orderDto,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<OrderResponseDTO>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/order/{orderId}/pay")
    public ResponseEntity<Void> updateOrderStatusToPaid(@PathVariable("orderId") Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                order.setStatus("PAID");
                orderService.saveOrder(order);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        try {
            // Gọi hàm getAllOrderDetails mới
            List<OrderResponseDTO> orders = orderService.getAllOrderDetails();
            if (orders != null && !orders.isEmpty()) {
                return new ResponseEntity<List<OrderResponseDTO>>(
                        orders,
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<List<OrderResponseDTO>>(headerGenerator.getHeadersForError(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<List<OrderResponseDTO>>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/order/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable("userId") Long userId) {
        try {
            List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);

            // Trả về 200 OK kể cả khi danh sách rỗng để Frontend check mảng rỗng hiển thị
            // màn hình "Chưa có đơn hàng"
            return new ResponseEntity<>(
                    orders,
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    headerGenerator.getHeadersForError(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 🌟 FIX BUG 2: ĐỔI SANG NHẬN @RequestBody Map
    @PutMapping(value = "/order/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> payload) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                order.setStatus(payload.get("status"));
                orderService.saveOrder(order);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
    }
}