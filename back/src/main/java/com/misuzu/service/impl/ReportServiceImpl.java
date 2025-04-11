package com.misuzu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.misuzu.model.dto.ReportModelRequest;
import com.misuzu.model.dto.ReportModelResponse;
import com.misuzu.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.PageSize;

/**
 * 报告服务实现类
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Value("bce-v3/ALTAK-Gt7uW3i1DPt574mxdNJ4h/28abd2d42cbeb5fd624db7ac634fa73a09a40c2e")
    private String apiKey;

    @Value("https://qianfan.baidubce.com")
    private String baseUrl;
    
    @Value("/v2/chat/completions")
    private String apiEndpoint;

    @Value("ernie-3.5-8k")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用大模型生成报告内容
     * 实际项目中，这里会调用外部AI服务，如文心大模型API
     * 
     * @param request 报告生成请求数据
     * @return 模型分析生成的报告内容
     */
    @Override
    public ReportModelResponse generateModelAnalysis(ReportModelRequest request) {
        log.info("开始生成报告内容，模板: {}, 训练类型: {}, 日期范围: {}", 
            request.getTemplate(), 
            request.getTrainingTypes(),
            request.getDateRange() != null ? Arrays.toString(request.getDateRange()) : "未指定");
        
        try {
            // 在实际项目中，这里会调用AI大模型API
            log.debug("准备调用AI大模型API");
            
            // 调用文心大模型API生成内容
            String content = callBaiduModelAPI(request);
            if (content != null) {
                log.debug("成功获取AI大模型生成内容");
                
                // 解析AI生成的内容，提取需要的信息
                return parseModelResponse(content, request);
            }
            
            log.warn("AI大模型返回内容为空，使用默认模拟数据");
            
            // 根据请求参数构建模拟的大模型分析结果
            String template = request.getTemplate();
            List<String> trainingTypes = request.getTrainingTypes();
            
            log.debug("开始根据训练类型生成报告章节");
            
            // 构造响应数据
            List<ReportModelResponse.Section> sections = new ArrayList<>();
            
            // 根据选择的训练类型生成对应章节
            if (trainingTypes != null) {
                if (trainingTypes.contains("forehand")) {
                    sections.add(ReportModelResponse.Section.builder()
                            .title("正手技术分析")
                            .content("正手击球动作规范度提升了15%，力量和速度也有明显提高。击球点位置把握更加准确，但在高速球处理时仍有不稳定情况。建议增加高速球应对训练，提高应变能力。")
                            .build());
                }
                
                if (trainingTypes.contains("backhand")) {
                    sections.add(ReportModelResponse.Section.builder()
                            .title("反手技术分析")
                            .content("反手技术相比上月有5%的提升，但仍是相对薄弱环节。反手拉球质量不够稳定，特别是处理旋转球时。建议加强反手基本功训练，增加对不同旋转球的适应性训练。")
                            .build());
                }
                
                if (trainingTypes.contains("footwork")) {
                    sections.add(ReportModelResponse.Section.builder()
                            .title("步法移动分析")
                            .content("步法移动速度有所提升，但在快速变向和大范围移动时仍显不足。建议增加专项体能训练和步法训练，提高移动速度和协调性。")
                            .build());
                }
                
                if (trainingTypes.contains("serve")) {
                    sections.add(ReportModelResponse.Section.builder()
                            .title("发球技术分析")
                            .content("发球质量有较大提升，旋转和落点控制能力明显加强。但发球变化仍不够丰富，对手容易适应。建议增加不同旋转和落点组合的发球训练，提高发球的多变性。")
                            .build());
                }
            }
            
            // 如果没有选择训练类型或生成的章节为空，则添加默认章节
            if (sections.isEmpty()) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("技术综合分析")
                        .content("通过对近期训练数据的分析，您的整体技术水平呈现上升趋势。球路多变性和战术意识有了明显提升，但在面对高强度对抗时技术稳定性仍需加强。建议在后续训练中增加高强度模拟比赛环节，提高实战能力。")
                        .build());
            }
            
            // 根据模板类型调整内容详细程度
            String reportTitle = "乒乓球技术提高训练分析报告";
            String summary = "本报告基于近期的训练数据和视频分析，对训练效果进行了全面评估，并提出有针对性的训练建议。通过分析发现，技术水平整体呈上升趋势，但各项技术发展不均衡，需要有针对性地加强训练。";
            String conclusion = "总体而言，训练效果良好，技术水平呈上升趋势。"
                    + (trainingTypes != null && trainingTypes.contains("forehand") ? "正手技术是最大优势，" : "")
                    + (trainingTypes != null && trainingTypes.contains("backhand") ? "反手技术和" : "")
                    + (trainingTypes != null && trainingTypes.contains("footwork") ? "步法移动是需要重点提升的方向。" : "")
                    + "建议保持现有训练强度，并针对薄弱环节进行专项训练。";
            
            String suggestions = "1. 每周安排2次反手专项训练，重点提高反手稳定性和应对旋转球的能力。\n"
                    + "2. 增加步法训练频率，每次训练前进行15分钟的专项步法练习。\n"
                    + "3. 安排1-2次对抗训练，提高实战应变能力。\n"
                    + "4. 使用视频录制训练过程，进行动作比对和纠正。";
            
            // 考虑用户已填写的内容
            if (request.getCurrentContent() != null) {
                // 如果用户已有内容，尽量保留或扩展
                if (request.getCurrentContent().getTitle() != null && !request.getCurrentContent().getTitle().isEmpty()) {
                    reportTitle = request.getCurrentContent().getTitle();
                }
                
                if (request.getCurrentContent().getSummary() != null && !request.getCurrentContent().getSummary().isEmpty()) {
                    summary = request.getCurrentContent().getSummary();
                }
                
                if (request.getCurrentContent().getConclusion() != null && !request.getCurrentContent().getConclusion().isEmpty()) {
                    conclusion = request.getCurrentContent().getConclusion();
                }
                
                if (request.getCurrentContent().getSuggestions() != null && !request.getCurrentContent().getSuggestions().isEmpty()) {
                    suggestions = request.getCurrentContent().getSuggestions();
                }
            }
            
            return ReportModelResponse.builder()
                    .title(reportTitle)
                    .summary(summary)
                    .sections(sections)
                    .conclusion(conclusion)
                    .suggestions(suggestions)
                    .build();
            
        } catch (Exception e) {
            log.error("生成报告内容出错", e);
            throw new RuntimeException("生成报告内容失败: " + e.getMessage());
        }
    }

    /**
     * 调用百度文心大模型API
     * 
     * @param request 报告请求对象
     * @return 大模型返回的内容
     */
    private String callBaiduModelAPI(ReportModelRequest request) {
        try {
            // 构建API请求URL
            String apiUrl = baseUrl + apiEndpoint;
            log.debug("API请求URL: {}", apiUrl);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建提示语
            String prompt = buildPrompt(request);
            log.debug("构建的提示语: {}", prompt);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的乒乓球教练和分析师，擅长分析训练数据并提供针对性的建议。请基于用户提供的信息生成一份专业的训练分析报告。");
            messages.add(systemMessage);
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.8);
            
            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            log.debug("发送API请求: {}", entity);
            
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
                log.debug("收到API响应状态码: {}", response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // 解析响应
                    JsonNode responseJson = objectMapper.readTree(response.getBody());
                    
                    if (responseJson.has("choices") && responseJson.get("choices").isArray() && 
                        responseJson.get("choices").size() > 0) {
                        
                        JsonNode firstChoice = responseJson.get("choices").get(0);
                        if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                            String content = firstChoice.get("message").get("content").asText();
                            log.debug("成功解析API响应内容");
                            return content;
                        }
                    }
                    
                    log.warn("无法从API响应中解析出有效内容: {}", response.getBody());
                } else {
                    log.error("API调用失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP客户端错误: {} - 状态码: {}, 响应: {}", e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
                log.error("请求URL: {}", apiUrl);
                log.error("请求头: {}", headers);
                log.error("请求体: {}", objectMapper.writeValueAsString(requestBody));
            } catch (org.springframework.web.client.RestClientException e) {
                log.error("REST客户端错误: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("调用文心大模型API出错: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 构建提示语
     * 
     * @param request 报告请求对象
     * @return 提示语
     */
    private String buildPrompt(ReportModelRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下信息生成一份乒乓球训练分析报告：\n\n");
        
        // 添加模板信息
        prompt.append("报告模板：").append(request.getTemplate()).append("\n");
        
        // 添加训练类型
        if (request.getTrainingTypes() != null && !request.getTrainingTypes().isEmpty()) {
            prompt.append("训练类型：").append(String.join("、", request.getTrainingTypes())).append("\n");
        }
        
        // 添加日期范围
        if (request.getDateRange() != null && request.getDateRange().length > 0) {
            prompt.append("分析日期范围：").append(Arrays.toString(request.getDateRange())).append("\n");
        }
        
        // 添加当前内容（如果有）
        if (request.getCurrentContent() != null) {
            prompt.append("\n当前已有内容：\n");
            
            if (request.getCurrentContent().getTitle() != null && !request.getCurrentContent().getTitle().isEmpty()) {
                prompt.append("标题：").append(request.getCurrentContent().getTitle()).append("\n");
            }
            
            if (request.getCurrentContent().getSummary() != null && !request.getCurrentContent().getSummary().isEmpty()) {
                prompt.append("摘要：").append(request.getCurrentContent().getSummary()).append("\n");
            }
            
            if (request.getCurrentContent().getSections() != null && !request.getCurrentContent().getSections().isEmpty()) {
                prompt.append("章节：\n");
                for (ReportModelRequest.Section section : request.getCurrentContent().getSections()) {
                    if (section.getTitle() != null && !section.getTitle().isEmpty()) {
                        prompt.append("- ").append(section.getTitle()).append("\n");
                    }
                }
            }
            
            if (request.getCurrentContent().getConclusion() != null && !request.getCurrentContent().getConclusion().isEmpty()) {
                prompt.append("结论：").append(request.getCurrentContent().getConclusion()).append("\n");
            }
            
            if (request.getCurrentContent().getSuggestions() != null && !request.getCurrentContent().getSuggestions().isEmpty()) {
                prompt.append("建议：").append(request.getCurrentContent().getSuggestions()).append("\n");
            }
        }
        
        prompt.append("\n请生成以下内容：\n");
        prompt.append("1. 报告标题\n");
        prompt.append("2. 报告摘要\n");
        prompt.append("3. 针对各训练类型的分析章节（每个章节包含标题和内容）\n");
        prompt.append("4. 总体结论\n");
        prompt.append("5. 训练建议\n");
        
        prompt.append("\n请以JSON格式返回，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"报告标题\",\n");
        prompt.append("  \"summary\": \"报告摘要\",\n");
        prompt.append("  \"sections\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"章节标题1\",\n");
        prompt.append("      \"content\": \"章节内容1\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"章节标题2\",\n");
        prompt.append("      \"content\": \"章节内容2\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"conclusion\": \"总体结论\",\n");
        prompt.append("  \"suggestions\": \"训练建议\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析模型响应
     * 
     * @param content 大模型返回的内容
     * @param request 报告请求对象
     * @return 报告响应对象
     */
    private ReportModelResponse parseModelResponse(String content, ReportModelRequest request) {
        try {
            // 预处理：移除可能导致JSON解析错误的字符
            String sanitizedContent = sanitizeJsonContent(content);
            
            // 尝试解析为JSON
            JsonNode jsonNode = objectMapper.readTree(sanitizedContent);
            
            // 提取各个字段
            String title = jsonNode.has("title") ? jsonNode.get("title").asText() : "乒乓球技术提高训练分析报告";
            String summary = jsonNode.has("summary") ? jsonNode.get("summary").asText() : "";
            String conclusion = jsonNode.has("conclusion") ? jsonNode.get("conclusion").asText() : "";
            String suggestions = jsonNode.has("suggestions") ? jsonNode.get("suggestions").asText() : "";
            
            // 提取章节
            List<ReportModelResponse.Section> sections = new ArrayList<>();
            if (jsonNode.has("sections") && jsonNode.get("sections").isArray()) {
                JsonNode sectionsNode = jsonNode.get("sections");
                for (JsonNode sectionNode : sectionsNode) {
                    String sectionTitle = sectionNode.has("title") ? sectionNode.get("title").asText() : "";
                    String sectionContent = sectionNode.has("content") ? sectionNode.get("content").asText() : "";
                    
                    sections.add(ReportModelResponse.Section.builder()
                            .title(sectionTitle)
                            .content(sectionContent)
                            .build());
                }
            }
            
            // 如果章节为空，添加一个默认章节
            if (sections.isEmpty()) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("综合分析")
                        .content("基于训练数据的综合分析结果。")
                        .build());
            }
            
            // 构建并返回响应对象
            return ReportModelResponse.builder()
                    .title(title)
                    .summary(summary)
                    .sections(sections)
                    .conclusion(conclusion)
                    .suggestions(suggestions)
                    .build();
            
        } catch (Exception e) {
            log.error("解析模型响应出错: {}", e.getMessage(), e);
            
            // 尝试从文本中提取信息
            try {
                return extractReportFromText(content, request);
            } catch (Exception ex) {
                log.error("从文本提取报告信息出错: {}", ex.getMessage(), ex);
                
                // 使用默认值构建响应
                return buildDefaultResponse(request);
            }
        }
    }
    
    /**
     * 从文本中提取报告信息
     * 
     * @param content 文本内容
     * @param request 报告请求对象
     * @return 报告响应对象
     */
    private ReportModelResponse extractReportFromText(String content, ReportModelRequest request) {
        // 简单的文本处理逻辑，尝试提取标题、摘要、章节等
        String[] lines = content.split("\n");
        
        String title = null;
        String summary = null;
        String conclusion = null;
        String suggestions = null;
        List<ReportModelResponse.Section> sections = new ArrayList<>();
        
        ReportModelResponse.Section currentSection = null;
        StringBuilder sectionContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("# ") || line.startsWith("标题：")) {
                // 提取标题
                title = line.replaceAll("^#\\s+|^标题：", "").trim();
            } else if (line.startsWith("## 摘要") || line.startsWith("摘要：")) {
                // 提取摘要
                summary = line.replaceAll("^##\\s+摘要|^摘要：", "").trim();
                if (summary.isEmpty() && lines.length > Arrays.asList(lines).indexOf(line) + 1) {
                    summary = lines[Arrays.asList(lines).indexOf(line) + 1].trim();
                }
            } else if (line.startsWith("## ") || line.startsWith("### ")) {
                // 如果已有正在处理的章节，保存它
                if (currentSection != null && sectionContent.length() > 0) {
                    currentSection.setContent(sectionContent.toString().trim());
                    sections.add(currentSection);
                    sectionContent = new StringBuilder();
                }
                
                // 创建新章节
                String sectionTitle = line.replaceAll("^##\\s+|^###\\s+", "").trim();
                currentSection = ReportModelResponse.Section.builder()
                        .title(sectionTitle)
                        .content("")
                        .build();
                
            } else if (line.startsWith("## 结论") || line.startsWith("结论：")) {
                // 提取结论
                conclusion = line.replaceAll("^##\\s+结论|^结论：", "").trim();
                if (conclusion.isEmpty() && lines.length > Arrays.asList(lines).indexOf(line) + 1) {
                    conclusion = lines[Arrays.asList(lines).indexOf(line) + 1].trim();
                }
            } else if (line.startsWith("## 建议") || line.startsWith("建议：")) {
                // 提取建议
                suggestions = line.replaceAll("^##\\s+建议|^建议：", "").trim();
                if (suggestions.isEmpty() && lines.length > Arrays.asList(lines).indexOf(line) + 1) {
                    suggestions = lines[Arrays.asList(lines).indexOf(line) + 1].trim();
                }
            } else if (currentSection != null) {
                // 添加内容到当前章节
                sectionContent.append(line).append("\n");
            }
        }
        
        // 保存最后一个章节
        if (currentSection != null && sectionContent.length() > 0) {
            currentSection.setContent(sectionContent.toString().trim());
            sections.add(currentSection);
        }
        
        // 使用提取的信息构建响应，如果某些信息缺失，使用默认值
        return ReportModelResponse.builder()
                .title(title != null ? title : "乒乓球技术提高训练分析报告")
                .summary(summary != null ? summary : "基于训练数据的分析报告")
                .sections(sections.isEmpty() ? createDefaultSections(request.getTrainingTypes()) : sections)
                .conclusion(conclusion != null ? conclusion : "综合分析了训练情况，需要持续改进")
                .suggestions(suggestions != null ? suggestions : "建议加强训练，提高技术水平")
                .build();
    }
    
    /**
     * 创建默认章节
     * 
     * @param trainingTypes 训练类型列表
     * @return 章节列表
     */
    private List<ReportModelResponse.Section> createDefaultSections(List<String> trainingTypes) {
        List<ReportModelResponse.Section> sections = new ArrayList<>();
        
        if (trainingTypes != null && !trainingTypes.isEmpty()) {
            if (trainingTypes.contains("forehand")) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("正手技术分析")
                        .content("正手击球动作规范度提升了15%，力量和速度也有明显提高。击球点位置把握更加准确，但在高速球处理时仍有不稳定情况。建议增加高速球应对训练，提高应变能力。")
                        .build());
            }
            
            if (trainingTypes.contains("backhand")) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("反手技术分析")
                        .content("反手技术相比上月有5%的提升，但仍是相对薄弱环节。反手拉球质量不够稳定，特别是处理旋转球时。建议加强反手基本功训练，增加对不同旋转球的适应性训练。")
                        .build());
            }
            
            if (trainingTypes.contains("footwork")) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("步法移动分析")
                        .content("步法移动速度有所提升，但在快速变向和大范围移动时仍显不足。建议增加专项体能训练和步法训练，提高移动速度和协调性。")
                        .build());
            }
            
            if (trainingTypes.contains("serve")) {
                sections.add(ReportModelResponse.Section.builder()
                        .title("发球技术分析")
                        .content("发球质量有较大提升，旋转和落点控制能力明显加强。但发球变化仍不够丰富，对手容易适应。建议增加不同旋转和落点组合的发球训练，提高发球的多变性。")
                        .build());
            }
        }
        
        // 如果没有选择训练类型或根据训练类型未能创建章节，添加默认章节
        if (sections.isEmpty()) {
            sections.add(ReportModelResponse.Section.builder()
                    .title("技术综合分析")
                    .content("通过对近期训练数据的分析，您的整体技术水平呈现上升趋势。球路多变性和战术意识有了明显提升，但在面对高强度对抗时技术稳定性仍需加强。建议在后续训练中增加高强度模拟比赛环节，提高实战能力。")
                    .build());
        }
        
        return sections;
    }
    
    /**
     * 构建默认响应
     * 
     * @param request 报告请求对象
     * @return 报告响应对象
     */
    private ReportModelResponse buildDefaultResponse(ReportModelRequest request) {
        String reportTitle = "乒乓球技术提高训练分析报告";
        String summary = "本报告基于近期的训练数据和视频分析，对训练效果进行了全面评估，并提出有针对性的训练建议。通过分析发现，技术水平整体呈上升趋势，但各项技术发展不均衡，需要有针对性地加强训练。";
        
        List<ReportModelResponse.Section> sections = createDefaultSections(request.getTrainingTypes());
        
        String conclusion = "总体而言，训练效果良好，技术水平呈上升趋势。"
                + (request.getTrainingTypes() != null && request.getTrainingTypes().contains("forehand") ? "正手技术是最大优势，" : "")
                + (request.getTrainingTypes() != null && request.getTrainingTypes().contains("backhand") ? "反手技术和" : "")
                + (request.getTrainingTypes() != null && request.getTrainingTypes().contains("footwork") ? "步法移动是需要重点提升的方向。" : "")
                + "建议保持现有训练强度，并针对薄弱环节进行专项训练。";
        
        String suggestions = "1. 每周安排2次反手专项训练，重点提高反手稳定性和应对旋转球的能力。\n"
                + "2. 增加步法训练频率，每次训练前进行15分钟的专项步法练习。\n"
                + "3. 安排1-2次对抗训练，提高实战应变能力。\n"
                + "4. 使用视频录制训练过程，进行动作比对和纠正。";
        
        // 如果用户已有内容，尽量保留
        if (request.getCurrentContent() != null) {
            if (request.getCurrentContent().getTitle() != null && !request.getCurrentContent().getTitle().isEmpty()) {
                reportTitle = request.getCurrentContent().getTitle();
            }
            
            if (request.getCurrentContent().getSummary() != null && !request.getCurrentContent().getSummary().isEmpty()) {
                summary = request.getCurrentContent().getSummary();
            }
            
            if (request.getCurrentContent().getConclusion() != null && !request.getCurrentContent().getConclusion().isEmpty()) {
                conclusion = request.getCurrentContent().getConclusion();
            }
            
            if (request.getCurrentContent().getSuggestions() != null && !request.getCurrentContent().getSuggestions().isEmpty()) {
                suggestions = request.getCurrentContent().getSuggestions();
            }
        }
        
        return ReportModelResponse.builder()
                .title(reportTitle)
                .summary(summary)
                .sections(sections)
                .conclusion(conclusion)
                .suggestions(suggestions)
                .build();
    }

    // 添加新的辅助方法处理内容清理
    private String sanitizeJsonContent(String content) {
        if (content == null) return null;
        
        // 第一步：检测内容是否为合法JSON，如果已经是，则直接返回
        try {
            objectMapper.readTree(content);
            return content; // 已经是有效JSON
        } catch (Exception ignored) {
            // 不是有效JSON，继续处理
        }
        
        // 第二步：提取JSON部分（很多AI模型会返回带有描述性文本的JSON）
        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher matcher = jsonPattern.matcher(content);
        if (matcher.find()) {
            content = matcher.group(0);
        }
        
        // 第三步：替换反引号和其他可能导致问题的字符
        return content.replaceAll("`", "")
                     .replaceAll("\\\\n", "\\n")
                     .replaceAll("\\s+", " ");
    }

    /**
     * 保存报告内容到数据库
     * 
     * @param reportData 报告数据
     * @return 保存的报告ID
     */
    @Override
    public Long saveReport(Object reportData) {
        try {
            log.info("保存报告数据: {}", objectMapper.writeValueAsString(reportData));
            
            // TODO: 实际项目中，这里应该将报告数据保存到数据库
            // 模拟数据库操作，返回一个随机ID
            Long reportId = System.currentTimeMillis();
            log.info("报告保存成功，ID: {}", reportId);
            
            return reportId;
        } catch (Exception e) {
            log.error("保存报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出报告为PDF
     * 
     * @param reportId 报告ID
     * @return PDF文件的字节数组
     */
    @Override
    public byte[] exportReportAsPdf(Long reportId) {
        log.info("开始导出报告PDF，报告ID: {}", reportId);
        
        try {
            // 模拟从数据库获取报告数据
            ReportModelResponse reportData = getMockReportData(reportId);
            
            // 创建PDF文档
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            
            // 添加中文字体支持
            BaseFont baseFont = null;
            try {
                // 尝试使用内置的亚洲字体
                baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                log.warn("无法加载STSong-Light字体，使用默认字体: {}", e.getMessage());
                // 使用默认字体
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            }
            
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headingFont = new Font(baseFont, 16, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            
            // 添加标题
            Paragraph title = new Paragraph(reportData.getTitle() != null ? reportData.getTitle() : "训练分析报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            
            // 添加生成日期
            Paragraph dateP = new Paragraph("生成日期: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), normalFont);
            dateP.setAlignment(Element.ALIGN_CENTER);
            document.add(dateP);
            document.add(Chunk.NEWLINE);
            
            // 添加摘要
            Paragraph summaryTitle = new Paragraph("摘要", headingFont);
            document.add(summaryTitle);
            document.add(new Paragraph(reportData.getSummary() != null ? reportData.getSummary() : "本报告包含训练分析结果。", normalFont));
            document.add(Chunk.NEWLINE);
            
            // 添加章节
            if (reportData.getSections() != null && !reportData.getSections().isEmpty()) {
                for (ReportModelResponse.Section section : reportData.getSections()) {
                    if (section.getTitle() != null && !section.getTitle().trim().isEmpty()) {
                        Paragraph sectionTitle = new Paragraph(section.getTitle(), headingFont);
                        document.add(sectionTitle);
                    }
                    document.add(new Paragraph(section.getContent() != null ? section.getContent() : "", normalFont));
                    document.add(Chunk.NEWLINE);
                }
            } else {
                document.add(new Paragraph("没有可用的章节内容", normalFont));
                document.add(Chunk.NEWLINE);
            }
            
            // 添加结论
            Paragraph conclusionTitle = new Paragraph("结论", headingFont);
            document.add(conclusionTitle);
            document.add(new Paragraph(reportData.getConclusion() != null ? reportData.getConclusion() : "通过分析，可以看出训练整体效果良好，但仍有提升空间。", normalFont));
            document.add(Chunk.NEWLINE);
            
            // 添加建议
            Paragraph suggestionsTitle = new Paragraph("训练建议", headingFont);
            document.add(suggestionsTitle);
            document.add(new Paragraph(reportData.getSuggestions() != null ? reportData.getSuggestions() : "建议加强技术动作的规范性训练，增加实战模拟练习。", normalFont));
            
            // 关闭文档
            document.close();
            
            byte[] pdfBytes = baos.toByteArray();
            log.info("PDF导出成功，大小: {} 字节", pdfBytes.length);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("PDF导出失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模拟报告数据（实际项目中应从数据库获取）
     * 
     * @param reportId 报告ID
     * @return 报告数据
     */
    private ReportModelResponse getMockReportData(Long reportId) {
        log.info("获取报告数据，ID: {}", reportId);
        
        // 创建模拟章节
        List<ReportModelResponse.Section> sections = new ArrayList<>();
        sections.add(ReportModelResponse.Section.builder()
                .title("正手技术分析")
                .content("正手击球动作规范度提升了15%，力量和速度也有明显提高。击球点位置把握更加准确，但在高速球处理时仍有不稳定情况。建议增加高速球应对训练，提高应变能力。")
                .build());
        
        sections.add(ReportModelResponse.Section.builder()
                .title("反手技术分析")
                .content("反手技术相比上月有5%的提升，但仍是相对薄弱环节。反手拉球质量不够稳定，特别是处理旋转球时。建议加强反手基本功训练，增加对不同旋转球的适应性训练。")
                .build());
        
        sections.add(ReportModelResponse.Section.builder()
                .title("步法移动分析")
                .content("步法移动速度有所提升，但在快速变向和大范围移动时仍显不足。建议增加专项体能训练和步法训练，提高移动速度和协调性。")
                .build());
        
        // 创建完整报告响应
        return ReportModelResponse.builder()
                .title("乒乓球训练分析报告")
                .summary("本报告基于近期的训练数据和视频分析，对训练效果进行了全面评估，并提出有针对性的训练建议。通过分析发现，技术水平整体呈上升趋势，但各项技术发展不均衡，需要有针对性地加强训练。")
                .sections(sections)
                .conclusion("总体而言，训练效果良好，技术水平呈上升趋势。正手技术是最大优势，反手技术和步法移动是需要重点提升的方向。建议保持现有训练强度，并针对薄弱环节进行专项训练。")
                .suggestions("1. 每周安排2次反手专项训练，重点提高反手稳定性和应对旋转球的能力。\n2. 增加步法训练频率，每次训练前进行15分钟的专项步法练习。\n3. 安排1-2次对抗训练，提高实战应变能力。\n4. 使用视频录制训练过程，进行动作比对和纠正。")
                .build();
    }
} 