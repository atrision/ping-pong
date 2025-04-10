package com.misuzu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求数据传输对象
 * 用于接收用户发送的消息
 */
@Data
@NoArgsConstructor
public class ChatRequest {
    
    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 会话ID，可为空（新会话）
     */
    private String sessionId;
} 