package com.example.sales.config;

import com.example.sales.entity.User;
import com.example.sales.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        
        // 获取用户角色
        User user = userService.findByUsername(username);
        if (user != null && user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                authorizationInfo.addRole(role.getName());
            });
        }
        
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = (String) token.getPrincipal();
        
        // 从数据库获取用户信息
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        
        // 创建认证信息
        return new SimpleAuthenticationInfo(
            username,
            user.getPassword(),
            ByteSource.Util.bytes(username), // 使用用户名作为盐值
            getName()
        );
    }
}
