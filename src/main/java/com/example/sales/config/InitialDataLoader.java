package com.example.sales.config;

import com.example.sales.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        // 初始化角色
        userService.initializeRoles();

        // 创建默认管理员账户
        userService.initializeAdminUser("admin", "admin123");
    }
}
