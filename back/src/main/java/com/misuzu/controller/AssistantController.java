package com.misuzu.controller;

import com.misuzu.dto.ApiResponse;
import com.misuzu.dto.ChatMessage;
import com.misuzu.dto.ChatRequest;
import com.misuzu.dto.ChatSessionResponse;
import com.misuzu.service.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * AI助手控制器
 * 处理AI助手相关请求
 */
@RestController
@RequestMapping({"/assistant", "/ai-assistant"}) // 支持两种URL路径
@RequiredArgsConstructor
@Slf4j
public class AssistantController {

    private final AssistantService assistantService;

    /**
     * 处理根路径请求 - 直接访问/assistant或/ai-assistant时触发
     * 此方法解决页面刷新/直接访问问题
     */
    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<String>> handleRoot() {
        log.info("收到AI助手根路径请求");
        return ResponseEntity.ok(ApiResponse.success("AI助手服务状态正常"));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        log.info("收到测试请求");
        return ResponseEntity.ok(ApiResponse.success("测试成功"));
    }

    /**
     * 发送消息并获取AI回复
     * 
     * @param chatRequest 聊天请求
     * @return AI回复的消息
     */
    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<ChatMessage>>> sendMessage(@Valid @RequestBody ChatRequest chatRequest) {
        log.info("收到聊天请求(响应式): {}", chatRequest);

        return assistantService.sendMessage(
                chatRequest.getMessage(),
                chatRequest.getSessionId()
        ).map(response -> ResponseEntity.ok(ApiResponse.success("消息发送成功", response)));
    }
    
    /**
     * 发送消息并获取流式AI回复（使用服务器发送事件SSE）
     * 实现真正的流式输出
     * 
     * @param chatRequest 聊天请求
     * @return 流式AI回复内容，使用ServerSentEvent包装
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamResponse(@Valid @RequestBody ChatRequest chatRequest) {
        log.info("收到流式聊天请求: {}", chatRequest);
        
        // 获取底层流式响应
        Flux<String> contentFlux = assistantService.getStreamingResponse(
                chatRequest.getMessage(),
                chatRequest.getSessionId()
        );
        
        // 转换为ServerSentEvent格式，前端可以更容易处理
        return contentFlux
                .map(chunk -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("message")
                        .data(chunk)
                        .build())
                .doOnSubscribe(subscription -> log.info("客户端订阅流式响应"))
                .doOnNext(sse -> {
                    if (log.isDebugEnabled()) {
                        log.debug("发送SSE事件: {}", sse.data());
                    }
                })
                // 添加心跳事件，防止连接超时
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(i -> ServerSentEvent.<String>builder()
                                .id("heartbeat")
                                .event("heartbeat")
                                .data("")
                                .build()))
                .doOnComplete(() -> log.info("流式响应完成"))
                .doOnError(error -> log.error("流式响应错误", error));
    }
    
    /**
     * 创建新的聊天会话
     * 
     * @return 新会话信息
     */
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> createSession() {
        log.info("创建新的聊天会话");
        
        ChatSessionResponse session = assistantService.createNewSession();
        
        return ResponseEntity.ok(ApiResponse.success("会话创建成功", session));
    }

    /**
     * 发送消息并获取流式AI回复（GET方法，支持EventSource API）
     * 此方法用于支持原生EventSource，因为它只支持GET请求
     * 
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return 流式AI回复内容
     */
    @GetMapping(value = "/stream-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamResponseGet(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId) {
        log.info("收到GET流式聊天请求: 消息={}，会话ID={}", message, sessionId);
        
        // 获取底层流式响应
        Flux<String> contentFlux = assistantService.getStreamingResponse(message, sessionId);
        
        // 转换为ServerSentEvent格式
        return contentFlux
                .map(chunk -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("message")
                        .data(chunk)
                        .build())
                .doOnSubscribe(subscription -> log.info("客户端通过GET请求订阅流式响应"))
                // 添加心跳事件
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(i -> ServerSentEvent.<String>builder()
                                .id("heartbeat")
                                .event("heartbeat")
                                .data("")
                                .build()))
                // 添加完成事件，让前端知道何时关闭连接
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .id("complete")
                                .event("complete")
                                .data("COMPLETED")
                                .build()
                ))
                .doOnComplete(() -> log.info("GET流式响应完成"));
    }
} 