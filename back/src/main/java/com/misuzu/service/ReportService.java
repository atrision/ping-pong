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
    
    /**
     * 保存报告内容
     * 
     * @param reportData 报告数据
     * @return 保存的报告ID
     */
    Long saveReport(Object reportData);
    
    /**
     * 导出报告为PDF
     * 
     * @param reportId 报告ID
     * @return PDF文件的字节数组
     */
    byte[] exportReportAsPdf(Long reportId);
} 