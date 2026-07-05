package com.rainbowforest.orderservice.service;

import com.rainbowforest.orderservice.domain.Item;
import com.rainbowforest.orderservice.domain.Product;
import com.rainbowforest.orderservice.feignclient.ProductClient;
import com.rainbowforest.orderservice.redis.CartRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigDecimal;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private CartRedisRepository cartRedisRepository;

    // 🌟 HÀM TIỆN ÍCH TỰ VIẾT: Lấy giá chính xác dựa trên Variant ID
    private BigDecimal calculateSubTotal(Product product, Long variantId, int quantity) {
        if (product == null) return BigDecimal.ZERO;
        BigDecimal priceToUse = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;

        if (variantId != null && product.getVariants() != null) {
            // Ép kiểu ép buộc bằng Stream/Map ở đây hơi lằng nhằng vì khác project, 
            // anh em dùng vòng for truyền thống dò tìm variant khớp ID cho chắc cốp.
            for (Object vObj : product.getVariants()) {
                try {
                    // Dùng reflection/Map vì JSON parse sang có thể bị thành Map
                    Map<String, Object> variantMap = (Map<String, Object>) vObj;
                    Long vId = Long.valueOf(variantMap.get("id").toString());
                    if (vId.equals(variantId)) {
                        priceToUse = new BigDecimal(variantMap.get("price").toString());
                        break;
                    }
                } catch (Exception e) {}
            }
        }
        return priceToUse.multiply(new BigDecimal(quantity));
    }

    @Override
    public void addItemToCart(String cartId, Long productId, Long variantId, Integer quantity) {
        Product product = productClient.getProductById(productId);
        Item item = new Item();
        item.setQuantity(quantity);
        item.setProductId(productId);
        item.setVariantId(variantId); // 🌟 Set ID biến thể
        item.setSubTotal(calculateSubTotal(product, variantId, quantity));
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
                    
                    // Gắn thêm thông tin Variant vào DTO để Frontend lấy tên hiển thị
                    if (item.getVariantId() != null && product.getVariants() != null) {
                        for (Object vObj : product.getVariants()) {
                            try {
                                Map<String, Object> variantMap = (Map<String, Object>) vObj;
                                Long vId = Long.valueOf(variantMap.get("id").toString());
                                if (vId.equals(item.getVariantId())) {
                                    // Hack tạm: Ném trọng lượng vào description hoặc ghi đè productName để hiển thị
                                    String weight = variantMap.get("weight").toString() + variantMap.get("unit").toString();
                                    product.setProductName(product.getProductName() + " - " + weight);
                                    
                                    // Ghi đè lại ảnh nếu biến thể có ảnh riêng
                                    if(variantMap.get("imageUrl") != null) {
                                         product.setImageUrl(variantMap.get("imageUrl").toString());
                                    }
                                    break;
                                }
                            } catch (Exception e) {}
                        }
                    }

                } catch (Exception e) {
                    Product fallback = new Product();
                    fallback.setId(item.getProductId());
                    fallback.setProductName("Sản phẩm không xác định");
                    fallback.setPrice(BigDecimal.ZERO);
                    dto.setProduct(fallback);
                }
                response.add(dto);
            }
        }
        return response;
    }

    @Override
    public void changeItemQuantity(String cartId, Long productId, Long variantId, Integer quantity) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            // 🌟 Check kỹ cả 2 ID
            boolean matchProduct = item.getProductId().equals(productId);
            boolean matchVariant = (variantId == null && item.getVariantId() == null) || (variantId != null && variantId.equals(item.getVariantId()));
            
            if(matchProduct && matchVariant){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                item.setQuantity(quantity);
                Product product = productClient.getProductById(productId);
                item.setSubTotal(calculateSubTotal(product, variantId, quantity));
                cartRedisRepository.addItemToCart(cartId, item);
                break;
            }
        }
    }

    @Override
    public void deleteItemFromCart(String cartId, Long productId, Long variantId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            boolean matchProduct = item.getProductId().equals(productId);
            boolean matchVariant = (variantId == null && item.getVariantId() == null) || (variantId != null && variantId.equals(item.getVariantId()));
            
            if(matchProduct && matchVariant){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                break;
            }
        }
    }

    @Override
    public boolean checkIfItemIsExist(String cartId, Long productId, Long variantId) {
        List<Item> cart = (List) cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            boolean matchProduct = item.getProductId().equals(productId);
            boolean matchVariant = (variantId == null && item.getVariantId() == null) || (variantId != null && variantId.equals(item.getVariantId()));
            
            if(matchProduct && matchVariant){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Item> getAllItemsFromCart(String cartId) {
        return (List)cartRedisRepository.getCart(cartId, Item.class);
    }

    @Override
    public void deleteCart(String cartId) {
        cartRedisRepository.deleteCart(cartId);
    }

    @Override
    public void accumulateItemQuantity(String cartId, Long productId, Long variantId, Integer quantityToAdd) {
        List<Item> cart = (List)cartRedisRepository.getCart(cartId, Item.class);
        for(Item item : cart){
            boolean matchProduct = item.getProductId().equals(productId);
            boolean matchVariant = (variantId == null && item.getVariantId() == null) || (variantId != null && variantId.equals(item.getVariantId()));
            
            if(matchProduct && matchVariant){
                cartRedisRepository.deleteItemFromCart(cartId, item);
                
                int newQuantity = item.getQuantity() + quantityToAdd;
                item.setQuantity(newQuantity);
                Product product = productClient.getProductById(productId);
                item.setSubTotal(calculateSubTotal(product, variantId, newQuantity));
                
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
        if (guestCart == null || guestCart.isEmpty()) return;

        List<Item> userCart = (List) cartRedisRepository.getCart(userCartId, Item.class);
        
        // Dùng chuỗi "productId-variantId" làm khóa để gộp đồ chính xác
        Map<String, Item> cartMap = new HashMap<>();

        if (userCart != null) {
            for (Item item : userCart) {
                String key = item.getProductId() + "-" + (item.getVariantId() != null ? item.getVariantId() : "0");
                cartMap.put(key, item);
            }
        }

        for (Item guestItem : guestCart) {
            String key = guestItem.getProductId() + "-" + (guestItem.getVariantId() != null ? guestItem.getVariantId() : "0");
            
            if (cartMap.containsKey(key)) {
                Item existingItem = cartMap.get(key);
                int newQuantity = existingItem.getQuantity() + guestItem.getQuantity();
                existingItem.setQuantity(newQuantity);
                
                Product product = productClient.getProductById(guestItem.getProductId());
                existingItem.setSubTotal(calculateSubTotal(product, guestItem.getVariantId(), newQuantity));
            } else {
                cartMap.put(key, guestItem);
            }
        }

        cartRedisRepository.deleteCart(userCartId);
        for (Item mergedItem : cartMap.values()) {
            cartRedisRepository.addItemToCart(userCartId, mergedItem);
        }

        cartRedisRepository.deleteCart(guestCartId);
    }
}