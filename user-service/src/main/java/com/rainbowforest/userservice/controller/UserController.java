package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.http.header.HeaderGenerator;
import com.rainbowforest.userservice.service.UserService;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private HeaderGenerator headerGenerator;

    @Autowired
    private UserRoleRepository userRoleRepository; 
    
    @GetMapping (value = "/users")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users =  userService.getAllUsers();
        if(!users.isEmpty()) {
            return new ResponseEntity<List<User>>(
                users,
                headerGenerator.getHeadersForSuccessGetMethod(),
                HttpStatus.OK);
        }
        return new ResponseEntity<List<User>>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @GetMapping (value = "/users", params = "name")
    public ResponseEntity<User> getUserByName(@RequestParam("name") String userName){
        User user = userService.getUserByName(userName);
        if(user != null) {
            return new ResponseEntity<User>(
                    user,
                    headerGenerator.
                    getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<User>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @GetMapping (value = "/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id){
        User user = userService.getUserById(id);
        if(user != null) {
            return new ResponseEntity<User>(
                    user,
                    headerGenerator.
                    getHeadersForSuccessGetMethod(),
                    HttpStatus.OK);
        }
        return new ResponseEntity<User>(
                headerGenerator.getHeadersForError(),
                HttpStatus.NOT_FOUND);
    }

    @PostMapping (value = "/users")
    public ResponseEntity<User> addUser(@RequestBody User user, HttpServletRequest request){
        if(user != null)
            try {
                if (user.getRole() != null && user.getRole().getId() != null) {
                    UserRole realRole = userRoleRepository.findById(user.getRole().getId()).orElse(null);
                    user.setRole(realRole);
                }
                // --------------------------------
                
                userService.saveUser(user);
                return new ResponseEntity<User>(
                        user,
                        headerGenerator.getHeadersForSuccessPostMethod(request, user.getId()),
                        HttpStatus.CREATED);
            }catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            try {
                // Xóa mềm: Chỉ chuyển active = 0 (Khóa tài khoản)
                user.setActive(0);
                userService.saveUser(user);
                
                return new ResponseEntity<Void>(
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<Void>(
                        headerGenerator.getHeadersForError(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<Void>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User userDetails, HttpServletRequest request) {
        User existingUser = userService.getUserById(id);
        if (existingUser != null) {
            try {
                // 1. CHỈ CẬP NHẬT TRẠNG THÁI (Khóa / Mở khóa tài khoản)
                existingUser.setActive(userDetails.getActive());
                
                // 2. CHỈ CẬP NHẬT QUYỀN (Phân quyền nhân viên / khách hàng)
                // --- FIX LỖI ROLE_ID BỊ NULL ---
                if (userDetails.getRole() != null && userDetails.getRole().getId() != null) {
                    UserRole realRole = userRoleRepository.findById(userDetails.getRole().getId()).orElse(null);
                    existingUser.setRole(realRole);
                }
                userService.saveUser(existingUser);
                
                return new ResponseEntity<User>(
                        existingUser,
                        headerGenerator.getHeadersForSuccessPostMethod(request, existingUser.getId()),
                        HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<User>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<User>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }
}