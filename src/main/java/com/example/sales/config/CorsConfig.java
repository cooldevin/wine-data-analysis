package com.example.sales.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class CorsConfig implements WebMvcConfigurer {
  // 一天
  private static final long MAX_AGE = 24 * 60 * 60;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")   // 对当前路径下的所有请求都应用当前的跨域配置
              .allowedOrigins("*") // 允许的跨域源，可以通过逗号隔开，也可以用 * 表示允许所有源；
              .allowedMethods("*") // 允许的请求方法，可以指定具体的，如："GET"、"POST"、"PUT"、"DELETE"
              .allowedHeaders("*") // 允许的请求头类型，可以指定具体的，如："Content-Type", "Authorization
              .maxAge(MAX_AGE);    // 设置请求最大有效时长，在这个时长内，重复的请求就不会发送预检请求
  }
   
}