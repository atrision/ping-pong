package com.misuzu.service;

import com.misuzu.model.dto.ReportModelRequest;
import com.misuzu.model.dto.ReportModelResponse;

/**
 * 报告服务接口
 */
public interface ReportService {
    
    /**
     * 使用大模型生成报告内容
     * 
     * @param request 报告生成请求数据
     * @return 模型分析生成的报告内容
     */
    ReportModelResponse generateModelAnalysis(ReportModelRequest request);
} 