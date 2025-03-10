package com.example.sales.aspect;

import com.example.sales.annotation.Loggable;
import com.example.sales.entity.OperationLog;
import com.example.sales.repository.OperationLogRepository;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Around("@annotation(loggable)")
    public Object logOperation(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        String username = getCurrentUsername();
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String params = Arrays.toString(joinPoint.getArgs());
        
        Object result = null;
        try {
            // 执行原方法
            result = joinPoint.proceed();
            
            // 记录操作日志
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setOperation(loggable.operation());
            log.setMethod(methodName);
            log.setParams(params);
            log.setExecuteTime(startTime);
            log.setStatus("SUCCESS");

            operationLogRepository.save(log);
            
            return result;
        } catch (Exception e) {
            // 记录失败日志
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setOperation(loggable.operation());
            log.setMethod(methodName);
            log.setParams(params);
            log.setExecuteTime(startTime);
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
            
            operationLogRepository.save(log);
            
            throw e;
        }
    }

    private String getCurrentUsername() {

        return (String) SecurityUtils.getSubject().getPrincipal();
    }


}
