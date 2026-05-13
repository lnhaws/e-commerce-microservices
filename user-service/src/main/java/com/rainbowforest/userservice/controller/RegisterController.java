package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.http.header.HeaderGenerator;
import com.rainbowforest.userservice.service.UserService;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
public class RegisterController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired
    private UserRoleRepository userRoleRepository; 
    
    @PostMapping(value = "/registration")
    public ResponseEntity<User> addUser(@RequestBody User user, HttpServletRequest request){
        if(user != null) {
            try {
                // ĐỒNG BỘ 1: CẤP QUYỀN AN TOÀN TRỰC TIẾP (CHUẨN BACKEND)
                // Chặn đứng lỗi Hardcode ID: Ta tìm quyền bằng chữ "User", ID có đổi cũng không sao!
                UserRole defaultRole = userRoleRepository.findUserRoleByRoleName("User");
                
                if (defaultRole == null) {
                    // Nếu gặp lỗi này, chứng tỏ trong DB của ông CHƯA CÓ quyền chữ "User".
                    // Ông cần mở SQL Server lên và Insert chữ "User" vào bảng role nhé!
                    System.out.println("CẢNH BÁO: Không tìm thấy quyền 'User' trong Database!");
                    return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                user.setRole(defaultRole);
                
                // ĐỒNG BỘ 2: Gán cứng active = 1 luôn theo ý Frontend cho chắc ăn
                user.setActive(1);

                // Lưu xuống DB (Tầng Service bây giờ chỉ việc lo Băm Mật Khẩu BCrypt)
                userService.saveUser(user);
                
                // ĐỒNG BỘ 3: CHE MẬT KHẨU
                user.setUserPassword("");

                return new ResponseEntity<User>(
                        user,
                        headerGenerator.getHeadersForSuccessPostMethod(request, user.getId()),
                        HttpStatus.CREATED);
            } catch (Exception e) {
                // In lỗi ra màn hình để anh em dễ bắt bệnh
                e.printStackTrace(); 
                return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }
}