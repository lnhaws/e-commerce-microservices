package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.getOne(id);
    }

    @Override
    public User getUserByName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public User saveUser(User user) {
        if (user.getRole() == null) {
            UserRole defaultRole = userRoleRepository.findUserRoleByRoleName("User");
            user.setRole(defaultRole);
        }

        if (user.getActive() == null) {
            user.setActive(1); 
        }

        // THÊM ĐOẠN NÀY: Kiểm tra nếu là thêm mới (id = null) thì mới đem băm mật khẩu
        // (Tránh trường hợp Update User bị băm lại mật khẩu cũ 2 lần)
        if (user.getId() == null) {
            String hashedPassword = passwordEncoder.encode(user.getUserPassword());
            user.setUserPassword(hashedPassword);
        }

        return userRepository.save(user);
    }
}