/*
 * @Author: misuzu 1411498103@qq.com
 * @Date: 2025-03-22 15:47:23
 * @LastEditors: misuzu 1411498103@qq.com
 * @LastEditTime: 2025-03-23 16:35:11
 * @FilePath: \网页端\back\src\main\java\com\misuzu\controller\AuthController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.misuzu.controller;

import com.misuzu.dto.ApiResponse;
import com.misuzu.dto.LoginRequest;
import com.misuzu.dto.LoginResponse;
import com.misuzu.dto.RegisterRequest;
import com.misuzu.repository.UserRepository;
import com.misuzu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册、登录等认证相关的请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应，包含用户信息和JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
    }
    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        ApiResponse<?> response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsernameAvailable(@RequestParam String username) {
        log.info("检查用户名是否可用: {}", username);
        
        boolean exists = userRepository.existsByUsername(username);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", !exists);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmailAvailable(@RequestParam String email) {
        log.info("检查邮箱是否可用: {}", email);
        
        boolean exists = userRepository.existsByEmail(email);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", !exists);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 简单登录测试
     * 
     * @return 登录测试结果
     */
    @GetMapping("/test-login")
    public String testLogin() {
        log.info("测试登录接口被调用");
        return "登录测试接口正常";
    }
} 