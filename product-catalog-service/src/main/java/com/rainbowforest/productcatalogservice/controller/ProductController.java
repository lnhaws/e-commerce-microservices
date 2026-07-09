package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.Product;
import com.rainbowforest.productcatalogservice.entity.ProductVariant;
import com.rainbowforest.productcatalogservice.http.header.HeaderGenerator;
import com.rainbowforest.productcatalogservice.service.ProductService;
import com.rainbowforest.productcatalogservice.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired
    private ProductVariantRepository productVariantRepository;
    private String getUploadPath() {
        String currentPath = System.getProperty("user.dir");
        if (!currentPath.endsWith("product-catalog-service")) {
            currentPath = currentPath + "/product-catalog-service";
        }
        return currentPath + "/uploads/";
    }

    @GetMapping (value = "/products")
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products =  productService.getAllProduct();
        if(!products.isEmpty()) {
            return new ResponseEntity<List<Product>>(
                    products,
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<List<Product>>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);       
    }

    @GetMapping(value = "/products", params = "categoryId")
    public ResponseEntity<List<Product>> getAllProductByCategoryId(@RequestParam ("categoryId") Long categoryId){
        List<Product> products = productService.getAllProductByCategoryId(categoryId);
        if(!products.isEmpty()) {
            return new ResponseEntity<List<Product>>(
                    products,
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<List<Product>>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @GetMapping("/products/category/{categoryId}/related/{excludeId}")
    public ResponseEntity<List<Product>> getRelatedProducts(
            @PathVariable("categoryId") Long categoryId, 
            @PathVariable("excludeId") Long excludeId) {
        
        List<Product> allInCategory = productService.getAllProductByCategoryId(categoryId);
        
        if (allInCategory == null || allInCategory.isEmpty()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }

        // Dùng Stream lọc bỏ sản phẩm hiện tại và chỉ lấy đúng 4 cái ném về Frontend
        List<Product> related = allInCategory.stream()
                .filter(p -> p.getId() != excludeId)
                .limit(4) 
                .collect(Collectors.toList());

        return new ResponseEntity<>(related, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping (value = "/products/{id}")
    public ResponseEntity<Product> getOneProductById(@PathVariable ("id") long id){
        Product product =  productService.getProductById(id);
        if(product != null) {
            return new ResponseEntity<Product>(
                    product,
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<Product>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @GetMapping (value = "/products", params = "name")
    public ResponseEntity<List<Product>> getAllProductsByName(@RequestParam ("name") String name){
        List<Product> products =  productService.getAllProductsByName(name);
        if(!products.isEmpty()) {
            return new ResponseEntity<List<Product>>(
                    products,
                    headerGenerator.getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<List<Product>>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @PostMapping("/admin/products")
    public ResponseEntity<Product> addProduct(
            @Valid @RequestBody Product product,
            HttpServletRequest request) {
        
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(product);
            }
        }
        Product savedProduct = productService.addProduct(product);
        return new ResponseEntity<>(
                savedProduct, 
                headerGenerator.getHeadersForSuccessPostMethod(request, savedProduct.getId()), 
                HttpStatus.CREATED
        );
    }

  @PutMapping("/admin/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") Long id, 
            @Valid @RequestBody Product product) {
        
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }
        
        existingProduct.setProductName(product.getProductName());
        existingProduct.setCategoryId(product.getCategoryId());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setAvailability(product.getAvailability());

        java.util.Map<Long, String> oldImages = new java.util.HashMap<>();
        if (existingProduct.getVariants() != null) {
            for (ProductVariant oldVar : existingProduct.getVariants()) {
                if (oldVar.getId() != null && oldVar.getImageUrl() != null) {
                    oldImages.put(oldVar.getId(), oldVar.getImageUrl());
                }
            }
            existingProduct.getVariants().clear();
        }

        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(existingProduct); 
                
                if (variant.getId() != null && oldImages.containsKey(variant.getId())) {
                    variant.setImageUrl(oldImages.get(variant.getId()));
                }
                
                existingProduct.getVariants().add(variant);
            }
        }

        Product updatedProduct = productService.addProduct(existingProduct); 
        return new ResponseEntity<>(updatedProduct, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        Product product = productService.getProductById(id);
        if(product != null) {
            productService.deleteProduct(id);
            return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    @PostMapping("/admin/products/{productId}/image")
    public ResponseEntity<Product> uploadProductImage(
            @PathVariable("productId") Long productId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            String uploadDir = getUploadPath();
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            // Lưu file vào ổ cứng
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Lưu đường dẫn vào SQL Server
            product.setImageUrl("/uploads/" + fileName); 
            productService.addProduct(product);
            
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/variants/{variantId}/image")
    public ResponseEntity<ProductVariant> uploadVariantImage(
            @PathVariable("variantId") Long variantId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file) {
        try {
            ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
            if (variant == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            String uploadDir = getUploadPath();
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) directory.mkdirs();
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            variant.setImageUrl("/uploads/" + fileName); 
            productVariantRepository.save(variant);
            
            return new ResponseEntity<>(variant, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/products/{id}/deduct")
    public ResponseEntity<?> deductInventory(
            @PathVariable("id") Long id,
            @RequestParam(value = "variantId", required = false) Long variantId,
            @RequestParam("quantity") int quantity) {
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return new ResponseEntity<>("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND);
        }
        if (variantId != null && product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getId().equals(variantId)) {
                    int currentStock = variant.getAvailability();
                    int newStock = currentStock - quantity;
                    
                    if (newStock < 0) {
                        return new ResponseEntity<>("Không đủ số lượng trong kho!", HttpStatus.BAD_REQUEST);
                    }
                    
                    variant.setAvailability(newStock);
                    break;
                }
            }
        } else {
            int currentStock = product.getAvailability();
            int newStock = currentStock - quantity;
            
            if (newStock < 0) {
                return new ResponseEntity<>("Không đủ số lượng trong kho!", HttpStatus.BAD_REQUEST);
            }
            
            product.setAvailability(newStock);
        }

        productService.addProduct(product);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}