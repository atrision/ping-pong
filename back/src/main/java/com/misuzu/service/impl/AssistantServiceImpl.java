package com.misuzu.service.impl;

import com.misuzu.dto.ChatMessage;
import com.misuzu.dto.ChatSessionResponse;
import com.misuzu.service.AssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * AI助手服务实现类
 * 实现与AI助手交互的业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantServiceImpl implements AssistantService {

    private final ChatClient chatClient;
    
    // 系统提示词模板
    private static final String SYSTEM_PROMPT = """
            你是乒乓球运动分析系统的AI助手，专注于帮助用户解答乒乓球训练、技术、数据分析和系统使用方面的问题。
            
            关于系统功能：
            1. 轨迹分析与识别：系统可以分析乒乓球运动轨迹和运动员动作，上传视频后可获得详细分析结果。
            2. 训练数据可视化：通过图表直观展示训练数据和效果，包括折线图、雷达图、热力图等。
            3. 训练报告生成：可基于模板生成专业训练报告，支持PDF、Word等多种格式导出。
            4. 用户管理系统：支持管理员、教练、普通用户等多角色管理。
            
            回答要求：
            1. 回答要简洁专业，直接切入问题核心。
            2. 使用礼貌友好的语气，但不必过于正式。
            3. 当涉及系统操作指南时，提供清晰的步骤说明。
            4. 对于乒乓球技术问题，回答应体现专业知识和教学经验。
            5. 不确定的问题，可以提供合理的建议，但要表明这是建议而非确定答案。
            """;

    /**
     * 发送消息并获取AI回复（响应式方式）
     *
     * @param message   用户消息
     * @param sessionId 会话ID（可为空，表示新会话）
     * @return 回复消息的Mono包装，支持响应式编程
     */
    @Override
    public Mono<ChatMessage> sendMessage(String message, String sessionId) {
        log.info("接收到用户消息(响应式): {}，会话ID: {}", message, sessionId);
        
        return Mono.fromCallable(() -> {
            // 确保会话ID存在
            String finalSessionId = sessionId;
            if (finalSessionId == null || finalSessionId.trim().isEmpty()) {
                finalSessionId = UUID.randomUUID().toString();
                log.info("创建新的会话ID: {}", finalSessionId);
            }
            return finalSessionId;
        })
        .flatMap(finalSessionId -> {
            try {
                // 使用流式API获取响应
                log.info("开始响应式流式调用, 会话ID: {}", finalSessionId);
                
                // 收集所有响应块为一个消息
                Mono<String> responseContentMono = chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(message)
                        .advisors(a -> {
                            a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, finalSessionId);
                            a.param("stream", true); // 明确指定流式输出
                        })
                        .stream()
                        .content()
                        .doOnNext(chunk -> log.debug("流式响应块: {}", chunk))
                        .reduce((accumulated, chunk) -> accumulated + chunk)
                        .doOnSuccess(result -> log.info("流式响应完整内容已收集完成, 长度: {}", result.length()));
                
                // 将内容转换为ChatMessage对象
                return responseContentMono.map(content -> ChatMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .content(content)
                        .timestamp(LocalDateTime.now())
                        .role("assistant")
                        .build());
            } catch (Exception e) {
                log.error("AI助手响应式回复失败", e);
                return Mono.just(ChatMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .content("抱歉，AI助手暂时无法回答您的问题，请稍后再试。")
                        .timestamp(LocalDateTime.now())
                        .role("assistant")
                        .build());
            }
        })
        .onErrorResume(e -> {
            log.error("处理用户消息时发生错误", e);
            return Mono.just(ChatMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .content("抱歉，处理您的请求时发生错误，请稍后再试。")
                    .timestamp(LocalDateTime.now())
                    .role("assistant")
                    .build());
        });
    }
    
    /**
     * 获取流式AI回复
     * 直接返回原始流式内容，由前端处理显示
     * 
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return 流式AI回复内容
     */
    @Override
    public Flux<String> getStreamingResponse(String message, String sessionId) {
        log.info("接收到流式聊天请求: {}，会话ID: {}", message, sessionId);
        
        try {
            // 确保会话ID存在
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
                log.info("创建新的会话ID: {}", sessionId);
            }

            // 使用正确的流式API返回响应
            final String finalSessionId = sessionId;

            // 记录开始生成流式响应
            log.info("开始生成流式响应, 会话ID: {}", finalSessionId);

            // 使用流式ChatClient响应
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .advisors(a -> {
                        a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, finalSessionId);
                        a.param("stream", true); // 明确指定流式输出
                    })
                    .stream()
                    .content()
                    // 分块处理，确保每个块都被发送
                    .delayElements(Duration.ofMillis(50)) // 轻微延迟，确保逐字显示效果
                    .doOnNext(chunk -> {
                        log.debug("流式响应块: {}", chunk);
                    })
                    .doOnComplete(() -> log.info("流式响应完成, 会话ID: {}", finalSessionId))
                    .doOnError(error -> log.error("流式响应错误", error));
        } catch (Exception e) {
            log.error("AI助手流式回复失败", e);
            return Flux.just("抱歉，AI助手暂时无法回答您的问题，请稍后再试。");
        }
    }

    /**
     * 创建新的聊天会话
     *
     * @return 新会话信息
     */
    @Override
    public ChatSessionResponse createNewSession() {
        String sessionId = UUID.randomUUID().toString();
        log.info("创建新的聊天会话: {}", sessionId);
        
        return ChatSessionResponse.builder()
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
} 