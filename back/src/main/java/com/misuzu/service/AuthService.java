package com.misuzu.service;

import com.misuzu.dto.ApiResponse;
import com.misuzu.dto.LoginRequest;
import com.misuzu.dto.LoginResponse;
import com.misuzu.dto.RegisterRequest;

/**
 * 认证服务接口
 * 定义用户认证相关的业务逻辑方法
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应，包含用户信息和JWT令牌
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return API响应，包含注册结果
     */
    ApiResponse<?> register(RegisterRequest registerRequest);
} 