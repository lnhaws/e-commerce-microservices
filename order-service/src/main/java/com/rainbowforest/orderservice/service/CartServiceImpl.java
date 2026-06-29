package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.dto.ItemResponseDTO;
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
        Item item = new Item();
        item.setQuantity(quantity);
        item.setProductId(productId);
        item.setSubTotal(CartUtilities.getSubTotalForItem(product, quantity));
        cartRedisRepository.addItemToCart(cartId, item);
    }

    @Override
    public List<Object> getCart(String cartId) {
        List<Item> items = (List<Item>)(List<?>) cartRedisRepository.getCart(cartId, Item.class);
        List<Object> response = new ArrayList<>();
        
        if (items != null) {
            for (Item item : items) {
                com.rainbowforest.orderservice.dto.ItemResponseDTO dto = new com.rainbowforest.orderservice.dto.ItemResponseDTO();
                dto.setId(item.getId());
                dto.setQuantity(item.getQuantity());
                dto.setSubTotal(item.getSubTotal());
                
                try {
                    Product product = productClient.getProductById(item.getProductId());
                    dto.setProduct(product);
                } catch (Exception e) {
                    System.out.println("Lỗi gọi Product Service: " + e.getMessage());
                    Product fallback = new Product();
                    fallback.setId(item.getProductId());
                    fallback.setProductName("Sản phẩm không xác định");
                    fallback.setPrice(java.math.BigDecimal.ZERO);
                    dto.setProduct(fallback);
                }
                response.add(dto);
            }
        }
        return response;
    }

    @Override
    public void changeItemQuantity(String cartId, Long productId, Integer quantity) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProductId()).equals(productId)){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                item.setQuantity(quantity);
                Product product = productClient.getProductById(productId);
                item.setSubTotal(CartUtilities.getSubTotalForItem(product, quantity));
                cartRedisRepository.addItemToCart(cartId, item);
            }
        }
    }

    @Override
    public void deleteItemFromCart(String cartId, Long productId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProductId()).equals(productId)){
                cartRedisRepository.deleteItemFromCart(cartId, item);
            }
        }
    }

    @Override
    public boolean checkIfItemIsExist(String cartId, Long productId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            if((item.getProductId()).equals(productId)){
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
            if((item.getProductId()).equals(productId)){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                
                int newQuantity = item.getQuantity() + quantityToAdd;
                item.setQuantity(newQuantity);
                Product product = productClient.getProductById(productId);
                item.setSubTotal(CartUtilities.getSubTotalForItem(product, newQuantity));
                
                cartRedisRepository.addItemToCart(cartId, item);
                break; 
            }
        }
    }

    @Override
    public List<Item> getSelectedItemsFromCart(String cartId, List<Long> productIds) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        List<Item> selectedItems = new ArrayList<>();
        if (cart != null && productIds != null) {
            for (Item item : cart) {
                if (productIds.contains(item.getProductId())) {
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
                if (productIds.contains(item.getProductId())) {
                    cartRedisRepository.deleteItemFromCart(cartId, item);
                }
            }
        }
    }

    @Override
    public void mergeCart(String guestCartId, String userCartId) {
        List<Item> guestCart = (List) cartRedisRepository.getCart(guestCartId, Item.class);
        
        if (guestCart == null || guestCart.isEmpty()) {
            return;
        }

        List<Item> userCart = (List) cartRedisRepository.getCart(userCartId, Item.class);
        Map<Long, Item> cartMap = new HashMap<>();

        if (userCart != null) {
            for (Item item : userCart) {
                cartMap.put(item.getProductId(), item);
            }
        }

        for (Item guestItem : guestCart) {
            Long productId = guestItem.getProductId();
            
            if (cartMap.containsKey(productId)) {
                Item existingItem = cartMap.get(productId);
                int newQuantity = existingItem.getQuantity() + guestItem.getQuantity();
                existingItem.setQuantity(newQuantity);
                Product product = productClient.getProductById(productId);
                existingItem.setSubTotal(CartUtilities.getSubTotalForItem(product, newQuantity));
            } else {
                cartMap.put(productId, guestItem);
            }
        }

        cartRedisRepository.deleteCart(userCartId);
        for (Item mergedItem : cartMap.values()) {
            cartRedisRepository.addItemToCart(userCartId, mergedItem);
        }

        cartRedisRepository.deleteCart(guestCartId);
    }
}