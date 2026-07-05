package com.rainbowforest.orderservice.service;

import java.util.List;
import com.rainbowforest.orderservice.domain.Item;

public interface CartService {

    public void addItemToCart(String cartId, Long productId, Long variantId, Integer quantity);
    public List<Object> getCart(String cartId);
    public void changeItemQuantity(String cartId, Long productId, Long variantId, Integer quantity);
    public void deleteItemFromCart(String cartId, Long productId, Long variantId);
    public boolean checkIfItemIsExist(String cartId, Long productId, Long variantId);
    public List<Item> getAllItemsFromCart(String cartId);
    public void deleteCart(String cartId);
    public void accumulateItemQuantity(String cartId, Long productId, Long variantId, Integer quantityToAdd);
        public List<Item> getSelectedItemsFromCart(String cartId, List<Long> productIds);
    public void deleteSelectedItemsFromCart(String cartId, List<Long> productIds);
    public void mergeCart(String guestCartId, String userCartId);
}