package com.misuzu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话响应数据传输对象
 * 用于返回新创建的会话信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 