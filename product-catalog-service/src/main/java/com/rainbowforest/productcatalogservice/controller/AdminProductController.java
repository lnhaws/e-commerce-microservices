// package com.rainbowforest.productcatalogservice.controller;

// import com.rainbowforest.productcatalogservice.dto.CategoryDTO;
// import com.rainbowforest.productcatalogservice.feignclient.CategoryClient;
// import com.rainbowforest.productcatalogservice.entity.Product;
// import com.rainbowforest.productcatalogservice.http.header.HeaderGenerator;
// import com.rainbowforest.productcatalogservice.service.ProductService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import javax.servlet.http.HttpServletRequest;
// import java.io.File;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;

// @RestController
// @RequestMapping("/admin")
// public class AdminProductController {

//     @Autowired
//     private ProductService productService;
    
//     @Autowired
//     private HeaderGenerator headerGenerator;

//     // NHÚNG CÔNG CỤ GỌI ĐIỆN CHO CATEGORY SERVICE VÀO ĐÂY
//     @Autowired
//     private CategoryClient categoryClient;

//     @PostMapping(value = "/products")
//     public ResponseEntity<Product> addProduct(@RequestBody Product product, HttpServletRequest request){
//         if(product != null) {
//             try {
//                 // KIỂM TRA CATEGORY TRƯỚC KHI LƯU
//                 if (product.getCategoryId() != null) {
//                     CategoryDTO category = categoryClient.getCategoryById(product.getCategoryId());
//                     if (category == null) {
//                         return new ResponseEntity<Product>(
//                                 headerGenerator.getHeadersForError(), 
//                                 HttpStatus.BAD_REQUEST); // Danh mục không tồn tại
//                     }
//                 }

//                 productService.addProduct(product);
//                 return new ResponseEntity<Product>(
//                         product,
//                         headerGenerator.getHeadersForSuccessPostMethod(request, product.getId()),
//                         HttpStatus.CREATED);
//             }catch (Exception e) {
//                 e.printStackTrace();
//                 return new ResponseEntity<Product>(
//                         headerGenerator.getHeadersForError(),
//                         HttpStatus.INTERNAL_SERVER_ERROR);
//             }
//         }
//         return new ResponseEntity<Product>(
//                 headerGenerator.getHeadersForError(),
//                 HttpStatus.BAD_REQUEST);       
//     }
    
//     @DeleteMapping(value = "/products/{id}")
//     public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id){
//         Product product = productService.getProductById(id);
//         if(product != null) {
//             try {
//                 productService.deleteProduct(id);
//                 return new ResponseEntity<Void>(
//                         headerGenerator.getHeadersForSuccessGetMethod(),
//                         HttpStatus.OK);
//             }catch (Exception e) {
//                 e.printStackTrace();
//                 return new ResponseEntity<Void>(
//                         headerGenerator.getHeadersForError(),
//                         HttpStatus.INTERNAL_SERVER_ERROR);
//             }
//         }
//         return new ResponseEntity<Void>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);      
//     }

//     @PutMapping(value = "/products/{id}")
//     public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @RequestBody Product productDetails, HttpServletRequest request) {
//         Product existingProduct = productService.getProductById(id);
//         if (existingProduct != null) {
//             try {
//                 // KIỂM TRA CATEGORY TRƯỚC KHI CẬP NHẬT
//                 if (productDetails.getCategoryId() != null) {
//                     CategoryDTO category = categoryClient.getCategoryById(productDetails.getCategoryId());
//                     if (category == null) {
//                         return new ResponseEntity<Product>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
//                     }
//                     existingProduct.setCategoryId(productDetails.getCategoryId());
//                 }

//                 existingProduct.setProductName(productDetails.getProductName());
//                 existingProduct.setPrice(productDetails.getPrice());
//                 existingProduct.setDescription(productDetails.getDescription());
//                 existingProduct.setAvailability(productDetails.getAvailability());
                
//                 // Lưu đè lại vào cơ sở dữ liệu
//                 productService.addProduct(existingProduct); 
//                 return new ResponseEntity<Product>(
//                         existingProduct, 
//                         headerGenerator.getHeadersForSuccessPostMethod(request, existingProduct.getId()), 
//                         HttpStatus.OK);
//             } catch (Exception e) {
//                 e.printStackTrace();
//                 return new ResponseEntity<Product>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
//             }
//         }
//         return new ResponseEntity<Product>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
//     }

//     @PostMapping(value = "/products/{id}/image")
//     public ResponseEntity<?> uploadProductImage(@PathVariable("id") Long productId, 
//                                                 @RequestParam("image") MultipartFile file) {
//         try {
//             Product product = productService.getProductById(productId);
//             if(product == null) {
//                 return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
//             }

//             String currentPath = System.getProperty("user.dir");
//             if (!currentPath.endsWith("product-catalog-service")) {
//                 currentPath = currentPath + "/product-catalog-service";
//             }
            
//             String uploadDir = currentPath + "/uploads/"; 
//             File directory = new File(uploadDir);
//             if (!directory.exists()) {
//                 directory.mkdirs();
//             }

//             String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//             Path filePath = Paths.get(uploadDir + fileName);
            
//             Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
//             String fileUrl = "/uploads/" + fileName;
            
//             product.setImageUrl(fileUrl); 
//             productService.addProduct(product); 
            
//             return ResponseEntity.ok("Up ảnh vào backend thành công! Link: " + fileUrl);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }
// }