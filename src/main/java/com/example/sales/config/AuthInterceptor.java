package com.example.sales.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import com.example.sales.entity.User;
import com.example.sales.service.UserService;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static org.apache.shiro.web.filter.mgt.DefaultFilter.user;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }



        // 修正为Shiro原生注解检查
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresRoles requiresRoles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        if (requiresRoles == null) {
            return true;
        }

        // 获取token的逻辑保持不变...

        // 修改为Shiro权限验证
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.checkRoles(requiresRoles.value());
        } catch (UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "缺少角色权限: " + Arrays.toString(requiresRoles.value()));
            return false;
        }

        request.setAttribute("currentUser", user);
        return true;
    }
}
