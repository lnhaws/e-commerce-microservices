package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.dto.LoginRequest;
import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.http.header.HeaderGenerator;
import com.rainbowforest.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
    public class LoginController {

        @Autowired
        private UserService userService;

        @Autowired
        private HeaderGenerator headerGenerator;

        // NHÚNG MÁY BĂM VÀO ĐÂY
        @Autowired
        private PasswordEncoder passwordEncoder;

        @PostMapping(value = "/login")
        public ResponseEntity<?> authenticateUser(
                @Valid @RequestBody LoginRequest loginRequest,
                HttpServletRequest request) {
            try {
                User existingUser = userService.getUserByName(loginRequest.getUserName());

                if (existingUser == null) {
                    return new ResponseEntity<>("Tài khoản không tồn tại!", headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND); 
                }

                if (!passwordEncoder.matches(loginRequest.getUserPassword(), existingUser.getUserPassword())) {
                    return new ResponseEntity<>("Sai mật khẩu!", headerGenerator.getHeadersForError(), HttpStatus.UNAUTHORIZED); 
                }

                if (existingUser.getActive() != null && existingUser.getActive() == 0) {
                    return new ResponseEntity<>("Tài khoản của bạn đã bị khóa!", headerGenerator.getHeadersForError(), HttpStatus.FORBIDDEN); 
                }

                return new ResponseEntity<User>(
                        existingUser,
                        headerGenerator.getHeadersForSuccessPostMethod(request, existingUser.getId()),
                        HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }