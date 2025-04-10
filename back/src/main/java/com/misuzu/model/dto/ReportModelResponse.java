package com.misuzu.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * 报告模型分析响应DTO
 */
@Data
@Builder
public class ReportModelResponse {
    
    /**
     * 报告标题
     */
    private String title;
    
    /**
     * 报告摘要
     */
    private String summary;
    
    /**
     * 报告章节
     */
    private List<Section> sections;
    
    /**
     * 结论
     */
    private String conclusion;
    
    /**
     * 训练建议
     */
    private String suggestions;
    
    /**
     * 章节内容类
     */
    @Data
    @Builder
    public static class Section {
        /**
         * 章节标题
         */
        private String title;
        
        /**
         * 章节内容
         */
        private String content;
    }
} 