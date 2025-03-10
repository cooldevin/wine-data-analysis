package com.example.sales.config;

import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.codec.Base64;
import javax.crypto.spec.SecretKeySpec;

public class CustomRememberMeManager extends CookieRememberMeManager {
    
    private static final String ALGORITHM = "AES";
    
    public CustomRememberMeManager() {
        String key = "YOUR_SECRET_KEY"; // 使用16、24或32字节的密钥
        byte[] keyBytes = key.getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        setCipherKey(keySpec.getEncoded());
    }
    
    protected byte[] getDecryptionKey() {
        return getCipherKey();
    }
    
    protected byte[] getEncryptionKey() {
        return getCipherKey();
    }
} 