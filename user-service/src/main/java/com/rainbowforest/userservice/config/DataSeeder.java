package com.rainbowforest.userservice.config;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        UserRole adminRole = userRoleRepository.findUserRoleByRoleName("Admin");
        if (adminRole == null) {
            adminRole = new UserRole();
            adminRole.setRoleName("Admin");
            userRoleRepository.save(adminRole);
            System.out.println("Đã tạo thành công Role: Admin");
        }

        UserRole userRole = userRoleRepository.findUserRoleByRoleName("User");
        if (userRole == null) {
            userRole = new UserRole();
            userRole.setRoleName("User");
            userRoleRepository.save(userRole);
            System.out.println("Đã tạo thành công Role: User");
        }

        User adminUser = userRepository.findByUserName("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUserName("admin");
            adminUser.setUserPassword(passwordEncoder.encode("123456")); 
            adminUser.setActive(1);
            
            adminUser.setRole(adminRole); 
            
            userRepository.save(adminUser);
            System.out.println("Tạo thành công tài khoản Admin mặc định (Tài khoản: admin | Mật khẩu: 123456)");
        }
    }
}