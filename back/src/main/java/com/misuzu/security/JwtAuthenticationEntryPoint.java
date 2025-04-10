package com.misuzu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misuzu.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证入口点
 * 处理认证异常，返回401未授权响应
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 处理未授权异常
     * 当用户尝试访问受保护的资源而没有提供有效认证时调用此方法
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param authException 认证异常
     * @throws IOException 输入输出异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // 创建错误响应
        ApiResponse<?> errorResponse = ApiResponse.error(
                HttpServletResponse.SC_UNAUTHORIZED,
                authException.getMessage() != null ? authException.getMessage() : "Unauthorized");
        
        // 写入响应体
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
} 