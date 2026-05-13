package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.feignclient.ProductClient;
import com.rainbowforest.orderservice.redis.CartRedisRepository;
import com.rainbowforest.orderservice.utilities.CartUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartRedisRepository cartRedisRepository;

    @Override
    public void addItemToCart(String cartId, Long productId, Integer quantity) {
        Product product = productClient.getProductById(productId);
        Item item = new Item(quantity,product, CartUtilities.getSubTotalForItem(product,quantity));
        cartRedisRepository.addItemToCart(cartId, item);
    }

    @Override
    public List<Object> getCart(String cartId) {
        return (List<Object>)cartRedisRepository.getCart(cartId, Item.class);
    }

    @Override
    public void changeItemQuantity(String cartId, Long productId, Integer quantity) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProduct().getId()).equals(productId)){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                item.setQuantity(quantity);
                item.setSubTotal(CartUtilities.getSubTotalForItem(item.getProduct(),quantity));
                cartRedisRepository.addItemToCart(cartId, item);
            }
        }
    }

    @Override
    public void deleteItemFromCart(String cartId, Long productId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProduct().getId()).equals(productId)){
                cartRedisRepository.deleteItemFromCart(cartId, item);
            }
        }
    }

    @Override
    public boolean checkIfItemIsExist(String cartId, Long productId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProduct().getId()).equals(productId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Item> getAllItemsFromCart(String cartId) {
        List<Item> items = (List)cartRedisRepository.getCart(cartId, Item.class);
        return items;
    }

    @Override
    public void deleteCart(String cartId) {
        cartRedisRepository.deleteCart(cartId);
    }

    @Override
    public void accumulateItemQuantity(String cartId, Long productId, Integer quantityToAdd) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProduct().getId()).equals(productId)){
                // Xóa món cũ đi
                cartRedisRepository.deleteItemFromCart(cartId, item);
                
                // Cộng dồn số cũ với số lượng mới truyền vào
                int newQuantity = item.getQuantity() + quantityToAdd;
                item.setQuantity(newQuantity);
                item.setSubTotal(CartUtilities.getSubTotalForItem(item.getProduct(), newQuantity));
                
                // Lưu lại món mới đã được update số lượng
                cartRedisRepository.addItemToCart(cartId, item);
                break; // Tìm thấy rồi thì thoát vòng lặp cho nhẹ máy
            }
        }
    }
    @Override
    public List<Item> getSelectedItemsFromCart(String cartId, List<Long> productIds) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        List<Item> selectedItems = new ArrayList<>();
        if (cart != null && productIds != null) {
            for (Item item : cart) {
                // Nếu ID của món hàng nằm trong danh sách được tick chọn
                if (productIds.contains(item.getProduct().getId())) {
                    selectedItems.add(item);
                }
            }
        }
        return selectedItems;
    }

    @Override
    public void deleteSelectedItemsFromCart(String cartId, List<Long> productIds) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        if (cart != null && productIds != null) {
            for (Item item : cart) {
                if (productIds.contains(item.getProduct().getId())) {
                    cartRedisRepository.deleteItemFromCart(cartId, item);
                }
            }
        }
    }

    @Override
    public void mergeCart(String guestCartId, String userCartId) {
        // 1. Lấy danh sách sản phẩm từ giỏ Guest
        List<Item> guestCart = (List) cartRedisRepository.getCart(guestCartId, Item.class);
        
        // Nếu giỏ khách vãng lai trống thì không cần làm gì cả, kết thúc luôn
        if (guestCart == null || guestCart.isEmpty()) {
            return;
        }

        // Lấy danh sách sản phẩm từ giỏ User hiện tại
        List<Item> userCart = (List) cartRedisRepository.getCart(userCartId, Item.class);

        // 2. Dùng Map để xử lý gộp nhanh O(n) (Key là ProductId, Value là Item)
        Map<Long, Item> cartMap = new HashMap<>();

        // Bỏ toàn bộ đồ của User hiện tại vào Map trước
        if (userCart != null) {
            for (Item item : userCart) {
                cartMap.put(item.getProduct().getId(), item);
            }
        }

        // 3. Duyệt giỏ Guest để gộp vào Map
        for (Item guestItem : guestCart) {
            Long productId = guestItem.getProduct().getId();
            
            if (cartMap.containsKey(productId)) {
                // TRƯỜNG HỢP TRÙNG ID: Cộng dồn số lượng và tính lại tiền
                Item existingItem = cartMap.get(productId);
                int newQuantity = existingItem.getQuantity() + guestItem.getQuantity();
                existingItem.setQuantity(newQuantity);
                existingItem.setSubTotal(CartUtilities.getSubTotalForItem(existingItem.getProduct(), newQuantity));
            } else {
                // TRƯỜNG HỢP MÓN MỚI: Thêm thẳng vào Map
                cartMap.put(productId, guestItem);
            }
        }

        // 4. Lưu lại giỏ hàng User mới vào Redis
        // Xóa sạch giỏ User cũ đi để ghi đè cho an toàn
        cartRedisRepository.deleteCart(userCartId);
        for (Item mergedItem : cartMap.values()) {
            cartRedisRepository.addItemToCart(userCartId, mergedItem);
        }

        // 5. Dọn dẹp bộ nhớ: Xóa sạch giỏ Guest sau khi gộp xong
        cartRedisRepository.deleteCart(guestCartId);
    }
}