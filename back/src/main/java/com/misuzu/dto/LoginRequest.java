package com.misuzu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求数据传输对象
 * 用于接收前端传来的登录信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * 用户名，不能为空
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码，不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;
} 