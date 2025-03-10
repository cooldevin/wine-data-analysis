package com.example.sales.config;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.codec.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        
        // 设置登录页面
        shiroFilterFactoryBean.setLoginUrl("/auth/login");
        // 设置登录成功后跳转的页面
        shiroFilterFactoryBean.setSuccessUrl("/");
        // 设置未授权页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/auth/unauthorized");

        // 配置访问权限
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 通用OPTIONS请求放行（需放在最前面）
        filterChainDefinitionMap.put("/**", "anon[OPTIONS]");
        
        // 精确放行OPTIONS预检请求
        filterChainDefinitionMap.put("/auth/login", "anon[OPTIONS]");
        filterChainDefinitionMap.put("/auth/register", "anon[OPTIONS]");
        
        // 其他过滤器配置
        filterChainDefinitionMap.put("/auth/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/customer/api/**", "authc");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/auth/unauthorized", "anon");
        
        // 登录注册相关接口允许匿名访问
        filterChainDefinitionMap.put("/auth/login", "anon");
        filterChainDefinitionMap.put("/auth/register", "anon");
        //filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/index.html", "anon");
        filterChainDefinitionMap.put("/", "authc");
        
        // 数据存储相关接口需要认证
        filterChainDefinitionMap.put("/data/storage/**", "authc");
        
        // 添加数据存储相关路径
        filterChainDefinitionMap.put("/data/storage/content", "authc");
        filterChainDefinitionMap.put("/data/storage", "authc");
        filterChainDefinitionMap.put("/api/storage/**", "authc");
        filterChainDefinitionMap.put("/sales/overview/dashboard", "authc");
        
        
        // 其他所有路径需要认证
        filterChainDefinitionMap.put("/**", "user");

       // shiroFilterFactoryBean.setSuccessUrl("/");  // 修改为具体的业务页面
//shiroFilterFactoryBean.setUnauthorizedUrl("/403"); 
        
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SecurityManager securityManager(Realm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setRememberMeManager(new CustomRememberMeManager());
        
        // 禁用 RememberMe
        // securityManager.setRememberMeManager(null);
        
        // 设置会话管理
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdUrlRewritingEnabled(false); // 禁用URL重写 JSESSIONID
        securityManager.setSessionManager(sessionManager);
        
        return securityManager;
    }

    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return userRealm;
    }

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("SHA-256");
        hashedCredentialsMatcher.setHashIterations(1024);
        // 是否存储散列后的密码为16进制，需要与存储的格式一致
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        // 是否使用盐值加密，这里为true，因为我们使用了用户名作为盐值
        hashedCredentialsMatcher.setHashSalted(true);
        return hashedCredentialsMatcher;
    }

    @Bean
    public CacheManager cacheManager() {
        MemoryConstrainedCacheManager cacheManager = new MemoryConstrainedCacheManager();
        return cacheManager;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        return chainDefinition;
    }

    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(rememberMeCookie());
        
        // 设置加密密钥，生成一个固定的key
        byte[] cipherKey = Base64.decode("4AvVhmFLUs0KTA3Kprsdag==");
        rememberMeManager.setCipherKey(cipherKey);
        
        return rememberMeManager;
    }

    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setMaxAge(86400); // 24小时
        cookie.setHttpOnly(true);
        return cookie;
    }
}
