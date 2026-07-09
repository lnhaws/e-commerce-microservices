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
import javax.validation.Valid;

@RestController
public class RegisterController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired
    private UserRoleRepository userRoleRepository; 
    
    @PostMapping(value = "/registration")
    public ResponseEntity<User> addUser(
            @Valid @RequestBody User user,
            HttpServletRequest request){
        if(user != null) {
            try {
                UserRole defaultRole = userRoleRepository.findUserRoleByRoleName("User");
                
                if (defaultRole == null) {
                    System.out.println("CẢNH BÁO: Không tìm thấy quyền 'User' trong Database!");
                    return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                user.setRole(defaultRole);
                
                user.setActive(1);

                userService.saveUser(user);
                
                user.setUserPassword("");

                return new ResponseEntity<User>(
                        user,
                        headerGenerator.getHeadersForSuccessPostMethod(request, user.getId()),
                        HttpStatus.CREATED);
            } catch (Exception e) {
                e.printStackTrace(); 
                return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }
}