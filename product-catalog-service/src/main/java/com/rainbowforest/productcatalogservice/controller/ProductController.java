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

    // ==========================================
    // CÁC API DÀNH CHO KHÁCH HÀNG (GET)
    // ==========================================

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

    // ==========================================
    // 🌟 CÁC API DÀNH CHO ADMIN (THÊM/SỬA/XÓA)
    // ==========================================

    @PostMapping("/admin/products")
    // 🌟 ĐÃ SỬA: Thêm tham số HttpServletRequest request vào hàm
    public ResponseEntity<Product> addProduct(@RequestBody Product product, HttpServletRequest request) {
        
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
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @RequestBody Product product) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }

        // 1. Cập nhật thông tin cha
        existingProduct.setProductName(product.getProductName());
        existingProduct.setCategoryId(product.getCategoryId());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice()); // Giữ tạm để code UI cũ không bị sập
        existingProduct.setAvailability(product.getAvailability()); // Giữ tạm

        // 2. Cập nhật danh sách con (Xóa khối lượng cũ, đắp khối lượng mới vào)
        if (existingProduct.getVariants() != null) {
            existingProduct.getVariants().clear();
        }
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(existingProduct); // Khai báo cha
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
            productService.deleteProduct(id); // (Nếu hàm bên Service tên khác thì sửa lại)
            return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // 🌟 API UPLOAD ẢNH RIÊNG CHO TỪNG KHỐI LƯỢNG
    // ==========================================
    
    // 🌟 1. API UP ẢNH CHO SẢN PHẨM GỐC (LƯU LOCAL)
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

    // 🌟 2. API UP ẢNH CHO TỪNG BIẾN THỂ (LƯU LOCAL)
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
    public ResponseEntity<Void> deductInventory(
            @PathVariable("id") Long id,
            @RequestParam(value = "variantId", required = false) Long variantId,
            @RequestParam("quantity") int quantity) {
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (variantId != null && product.getVariants() != null) {
            // Tìm đúng cái biến thể khách mua để trừ kho
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getId().equals(variantId)) {
                    int currentStock = variant.getAvailability();
                    int newStock = currentStock - quantity;
                    variant.setAvailability(newStock < 0 ? 0 : newStock);
                    break;
                }
            }
        } else {
            // Dành cho sản phẩm cũ không có biến thể
            int currentStock = product.getAvailability();
            int newStock = currentStock - quantity;
            product.setAvailability(newStock < 0 ? 0 : newStock);
        }

        productService.addProduct(product); // Lưu cập nhật xuống DB
        return new ResponseEntity<>(HttpStatus.OK);
    }
}