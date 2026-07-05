package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Collections; 
import javax.servlet.http.HttpServletRequest;

@RestController
public class CartController {

    @Autowired
    CartService cartService;
    
    @Autowired
    private HeaderGenerator headerGenerator;

    @GetMapping (value = "/cart")
    public ResponseEntity<List<Object>> getCart(@RequestHeader(value = "Cart-Id") String cartId){
        List<Object> cart = cartService.getCart(cartId);
        
        return new ResponseEntity<List<Object>>(
                cart,
                headerGenerator.getHeadersForSuccessGetMethod(),
                HttpStatus.OK);
    }

    // 🌟 ĐÃ SỬA: Hứng thêm variantId từ Frontend gửi xuống
    @PostMapping(value = "/cart", params = {"productId", "quantity"})
    public ResponseEntity<List<Object>> addItemToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "variantId", required = false) Long variantId, // Bắt biến thể ở đây
            @RequestParam("quantity") Integer quantity,
            @RequestHeader(value = "Cart-Id") String cartId,
            HttpServletRequest request) {
        
        List<Object> cart = cartService.getCart(cartId);
        if(cart != null) {
            if(cart.isEmpty()){
                // Chú ý: Ông sẽ phải vào file CartService sửa thêm tham số variantId cho hàm này
                cartService.addItemToCart(cartId, productId, variantId, quantity); 
            } else {
                // Chú ý: Hàm check này giờ phải check cả productId VÀ variantId (Khác khối lượng là 2 dòng khác nhau)
                if(cartService.checkIfItemIsExist(cartId, productId, variantId)){ 
                    cartService.accumulateItemQuantity(cartId, productId, variantId, quantity);
                } else {
                    cartService.addItemToCart(cartId, productId, variantId, quantity);
                }
            }
            
            return new ResponseEntity<List<Object>>(
                    cartService.getCart(cartId),
                    headerGenerator.getHeadersForSuccessPostMethod(request, Long.parseLong(cartId)),
                    HttpStatus.CREATED);
        }
        return new ResponseEntity<List<Object>>(
                headerGenerator.getHeadersForError(),
                HttpStatus.BAD_REQUEST);
    }

    // 🌟 ĐÃ SỬA LẠI DELETE CHO CHUẨN
    @DeleteMapping(value = "/cart", params = {"productId"})
    public ResponseEntity<Void> removeItemFromCart(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "variantId", required = false) Long variantId,
            @RequestHeader(value = "Cart-Id") String cartId){
        List<Object> cart = cartService.getCart(cartId);
        if(cart != null) {
            cartService.deleteItemFromCart(cartId, productId, variantId); // Nhớ thêm variantId vào Service
            return new ResponseEntity<Void>(
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<Void>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/cart/merge")
    public ResponseEntity<?> mergeCart(
            @RequestParam("guestCartId") String guestCartId,
            @RequestParam("userCartId") String userCartId) { 
        try {
            cartService.mergeCart(guestCartId, userCartId);
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Merge success"), 
                    headerGenerator.getHeadersForSuccessGetMethod(), 
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Merge failed"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}