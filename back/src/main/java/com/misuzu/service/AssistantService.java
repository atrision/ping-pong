package com.misuzu.service;

import com.misuzu.dto.ChatMessage;
import com.misuzu.dto.ChatSessionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AI助手服务接口
 * 定义与AI助手交互的业务逻辑方法
 */
public interface AssistantService {
    
    /**
     * 发送消息并获取AI回复（响应式方式）
     * 
     * @param message 用户消息
     * @param sessionId 会话ID（可为空，表示新会话）
     * @return 回复消息的Mono包装，支持响应式编程
     */
    Mono<ChatMessage> sendMessage(String message, String sessionId);
    
    /**
     * 获取流式AI回复
     * 
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return 流式AI回复内容
     */
    Flux<String> getStreamingResponse(String message, String sessionId);
    
    /**
     * 创建新的聊天会话
     * 
     * @return 新会话信息
     */
    ChatSessionResponse createNewSession();
} 