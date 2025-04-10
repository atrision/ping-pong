package com.misuzu.controller;

import com.misuzu.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公共控制器
 * 处理不需要认证的公共API
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final JdbcTemplate jdbcTemplate;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${server.port}")
    private String serverPort;

    /**
     * 获取系统状态
     * 
     * @return 系统状态信息
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("version", "1.0.0");
        status.put("timestamp", System.currentTimeMillis());
        status.put("message", "乒乓球运动分析系统服务端运行正常");
        status.put("applicationName", applicationName);
        status.put("port", serverPort);
        
        return ApiResponse.success("系统正常", status);
    }
    
    /**
     * 健康检查
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        
        try {
            // 检查数据库连接
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            boolean dbStatus = result != null && result == 1;
            
            Map<String, Object> dbHealth = new HashMap<>();
            dbHealth.put("status", dbStatus ? "UP" : "DOWN");
            health.put("database", dbHealth);
        } catch (Exception e) {
            Map<String, Object> dbHealth = new HashMap<>();
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            health.put("database", dbHealth);
            health.put("status", "DOWN");
        }
        
        health.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(health);
    }
} 