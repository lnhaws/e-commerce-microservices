package com.rainbowforest.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PutMapping("/order/{orderId}/pay")
    void updateOrderStatusToPaid(@PathVariable("orderId") Long orderId);

    @PutMapping("/order/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestBody Map<String, String> statusPayload);
}