package com.misuzu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * REST端点配置类
 * 用于注册一些测试端点，确保Spring MVC配置正确
 */
@Configuration
public class RestEndpointsConfig {

    /**
     * 注册测试端点
     * 
     * @return RouterFunction对象
     */
    @Bean
    public RouterFunction<ServerResponse> testEndpoints() {
        return RouterFunctions.route()
                .GET("/api-test", request -> ServerResponse.ok().body("API测试成功"))
                .GET("/report/simple-test", request -> ServerResponse.ok().body("报告服务测试成功"))
                .GET("/api/report/router-test", request -> ServerResponse.ok().body("报告路由测试成功"))
                .GET("/api/direct-test", request -> ServerResponse.ok().body("直接API测试成功"))
                .build();
    }
} 