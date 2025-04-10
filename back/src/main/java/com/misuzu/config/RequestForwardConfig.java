package com.misuzu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 请求转发配置
 * 用于处理特定的请求路径转发
 */
@Configuration
public class RequestForwardConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 添加一些基本的视图控制器转发
        registry.addViewController("/api/report-test").setViewName("forward:/report/test");
        registry.addViewController("/api/report-ping").setViewName("forward:/report/ping");
    }
} 