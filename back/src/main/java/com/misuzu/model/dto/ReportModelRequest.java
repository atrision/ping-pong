package com.misuzu.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 报告模型分析请求DTO
 */
@Data
public class ReportModelRequest {

    /**
     * 选择的模板
     */
    private String template;

    /**
     * 日期范围
     */
    private String[] dateRange;

    /**
     * 训练类型
     */
    private List<String> trainingTypes;

    /**
     * 选择的会话ID列表
     */
    private List<Long> sessions;

    /**
     * 当前已填写的内容
     */
    private CurrentContent currentContent;

    /**
     * 当前内容嵌套类
     */
    @Data
    public static class CurrentContent {
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
    }

    /**
     * 章节内容嵌套类
     */
    @Data
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