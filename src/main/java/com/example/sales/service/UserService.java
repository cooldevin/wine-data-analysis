package com.example.sales.service;

import com.example.sales.entity.Role;
import com.example.sales.entity.User;
import com.example.sales.repository.RoleRepository;
import com.example.sales.repository.UserRepository;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword, String salt) {
        String hashedInputPassword = new Sha256Hash(rawPassword, salt, 1024).toHex();
        return hashedInputPassword.equals(encodedPassword);
    }

    @Transactional
    public User register(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        String salt = user.getUsername(); // 使用用户名作为盐值
        String hashedPassword = new Sha256Hash(password, salt, 1024).toHex();
        user.setPassword(hashedPassword);

        // 为新用户分配默认角色
        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }
        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    @Transactional
    public void initializeRoles() {
        // 初始化管理员角色
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        // 初始化普通用户角色
        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }
    }

    @Transactional
    public void initializeAdminUser(String username, String password) {
        User admin = userRepository.findByUsername(username);
        if (admin == null) {
            admin = new User();
            admin.setUsername(username);
            String salt = admin.getUsername(); // 使用用户名作为盐值
            String hashedPassword = new Sha256Hash(password, salt, 1024).toHex();
            admin.setPassword(hashedPassword);
            
            Role adminRole = roleRepository.findByName("ADMIN");
            if (adminRole == null) {
                initializeRoles();
                adminRole = roleRepository.findByName("ADMIN");
            }
            
            admin.setRoles(Collections.singleton(adminRole));
            userRepository.save(admin);
        }
    }
}
