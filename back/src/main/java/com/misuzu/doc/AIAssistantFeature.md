# AI助手功能实现文档

## 功能概述

乒乓球运动分析系统的AI助手功能提供了专业的乒乓球知识问答服务，能够解答用户关于乒乓球训练、技术、数据分析和系统使用方面的问题。支持标准问答模式和流式输出模式。

## 实现架构

### 前端实现

前端使用Vue.js实现，主要组件包括：

1. `AIAssistant.vue`：主页面组件，包含聊天界面和交互逻辑
2. `ChatMessage.vue`：消息展示组件，负责渲染聊天消息
3. `ChatInput.vue`：消息输入组件，处理用户输入
4. `StreamingChatDemo.vue`：流式输出演示组件，展示AI回复实时生成过程

### 后端实现

后端使用Spring Boot实现，主要组件包括：

1. `AssistantController`：处理AI助手相关请求的控制器
2. `AssistantService`：实现与AI模型交互的业务逻辑
3. Spring AI集成：通过Spring AI集成千帆大模型API

## API接口

### 发送消息（标准模式）

- **URL**: `/assistant/send` 或 `/ai-assistant/send`
- **方法**: POST
- **请求体**:
  ```json
  {
    "message": "用户消息内容",
    "sessionId": "会话ID（可选）"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "消息发送成功",
    "data": {
      "id": "消息ID",
      "content": "AI回复内容",
      "timestamp": "2023-07-01T10:30:00",
      "role": "assistant"
    }
  }
  ```

### 发送消息（流式输出模式）

- **URL**: `/assistant/stream` 或 `/ai-assistant/stream`
- **方法**: POST
- **请求体**:
  ```json
  {
    "message": "用户消息内容",
    "sessionId": "会话ID（可选）"
  }
  ```
- **响应**: 
  - 内容类型: `text/event-stream`
  - 格式: 文本流，每个数据块代表AI回复的部分内容
  - 客户端需使用`fetch` API和`ReadableStream`接收流式响应

### 创建会话

- **URL**: `/assistant/session` 或 `/ai-assistant/session`
- **方法**: POST
- **响应**:
  ```json
  {
    "code": 200,
    "message": "会话创建成功",
    "data": {
      "sessionId": "新会话ID",
      "createdAt": "2023-07-01T10:25:00"
    }
  }
  ```

## 数据流程

### 标准模式

1. 用户在前端输入问题并发送
2. 前端通过Pinia状态管理存储用户消息，并调用API发送到后端
3. 后端接收消息，生成专业的提示词发送给AI模型
4. AI模型生成完整回复，后端接收并格式化
5. 后端返回AI回复，前端接收并展示

### 流式输出模式

1. 用户在前端输入问题并发送
2. 前端调用流式API发送消息到后端
3. 后端接收消息，生成专业的提示词发送给AI模型
4. AI模型生成回复，以流的形式发送给客户端
5. 前端使用ReadableStream API接收流数据，实时展示AI回复生成过程
6. 流结束后，将完整回复添加到消息列表

## AI提示词设计

为了让AI助手能够提供专业的乒乓球知识并适应系统功能，设计了专门的提示词模板：

```
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
```

## 技术实现注意点

### 流式输出实现

后端使用Spring WebFlux的`Flux`类型返回流式响应：

```java
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamResponse(@Valid @RequestBody ChatRequest chatRequest) {
    return assistantService.getStreamingResponse(
            chatRequest.getMessage(),
            chatRequest.getSessionId()
    );
}
```

Service层实现：

```java
public Flux<String> getStreamingResponse(String message, String sessionId) {
    final String finalSessionId = sessionId;
    return chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(message)
            .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, finalSessionId))
            .stream()
            .content();
}
```

前端使用`fetch` API和`ReadableStream`接收流数据：

```javascript
const response = await getStreamingResponse(message, sessionId);
const reader = response.body.getReader();
const decoder = new TextDecoder();

let reading = true;
while (reading) {
    const { value, done } = await reader.read();
    if (done) {
        reading = false;
    } else {
        const chunk = decoder.decode(value);
        streamingContent.value += chunk;
    }
}
```

### Spring AI整合

使用Spring AI 1.0.0-M6版本，API的正确使用方式：

```java
// 错误的调用方式 (旧方式)
String aiResponse = chatClient.prompt(formattedPrompt).content().trim();

// 错误的调用方式 (不兼容的API)
ChatResponse response = chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user(message)
        .build()
        .call();
String aiResponse = response.getResult().getOutput().getContent().trim();

// 正确的调用方式
final String sessionId = "会话ID";
String aiResponse = chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user(message)
        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
        .call()
        .content()
        .trim();
```

### 前端注意事项

1. **避免无限循环**：
   在处理流式数据时，避免使用`while(true)`这样的无限循环，应该使用有明确终止条件的循环。

2. **废弃的API**：
   Vue组件中应避免使用已废弃的slot属性，应使用最新的插槽语法。

## 后续扩展计划

1. **功能扩展**：
   - 添加历史消息存储和查询功能
   - 实现会话列表管理
   - 支持图片上传和分析

2. **性能优化**：
   - 实现会话缓存，减少重复问题的AI调用
   - 添加常见问题快速回复功能

3. **用户体验改进**：
   - 优化流式输出动画效果
   - 支持富文本和Markdown格式 