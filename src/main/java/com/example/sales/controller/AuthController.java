package com.example.sales.controller;

import com.example.sales.dto.AuthRequest;
import com.example.sales.entity.User;
import com.example.sales.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

   
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Subject subject = SecurityUtils.getSubject();

        // 添加参数验证
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }
        
        // 如果已经登录，直接返回成功
        if (subject.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("username", subject.getPrincipal());
            response.put("message", "已经登录");
            return ResponseEntity.ok(response);
        }

        try {
            // 创建 Token 时设置记住我
            UsernamePasswordToken token = new UsernamePasswordToken(
                request.getUsername(),
                request.getPassword()
            );
            token.setRememberMe(true);
            
            // 执行登录
            subject.login(token);
            
            // 登录成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("username", request.getUsername());
            response.put("message", "登录成功");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (UnknownAccountException e) {
            return ResponseEntity.badRequest().body("用户不存在");
        } catch (IncorrectCredentialsException e) {
            return ResponseEntity.badRequest().body("密码错误");
        } catch (LockedAccountException e) {
            return ResponseEntity.badRequest().body("账号已被锁定");
        } catch (AuthenticationException e) {
            // 打印异常堆栈,帮助排查具体原因
            e.printStackTrace();
            return ResponseEntity.badRequest().body("认证失败,原因: " + e.getMessage());
        }
    }

    

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            User user = userService.register(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("注册成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "auth/unauthorized";
    }

    @GetMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verifyAuth() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("username", subject.getPrincipal());
            response.put("authenticated", true);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("未登录");
    }
}
