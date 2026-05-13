package com.rainbowforest.productcatalogservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // TÌM ĐÚNG TỌA ĐỘ ĐỂ MỞ CỬA CHO BÊN NGOÀI VÀO XEM ẢNH
        String currentPath = System.getProperty("user.dir");
        if (!currentPath.endsWith("product-catalog-service")) {
            currentPath = currentPath + "/product-catalog-service";
        }
        
        // Nối thêm chữ uploads và lấy đường dẫn tuyệt đối (C:/.../product-catalog-service/uploads)
        String uploadPath = Paths.get(currentPath, "uploads").toFile().getAbsolutePath();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}