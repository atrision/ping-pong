/*
 * @Author: misuzu 1411498103@qq.com
 * @Date: 2025-04-02 15:50:19
 * @LastEditors: misuzu 1411498103@qq.com
 * @LastEditTime: 2025-04-10 17:27:12
 * @FilePath: \网页端\back\src\main\java\com\misuzu\controller\ReportController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.misuzu.controller;

import com.misuzu.common.ApiResponse;
import com.misuzu.model.dto.ReportModelRequest;
import com.misuzu.model.dto.ReportModelResponse;
import com.misuzu.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 报告相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "报告管理接口")
public class ReportController {

    private final ReportService reportService;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        log.info("ReportController初始化完成，路径: /report");
    }

    /**
     * 获取大模型分析结果接口
     *
     * @param request 报告模型请求
     * @return 大模型生成的报告内容
     */
    @PostMapping({"/model-analysis", "/v1/model-analysis"})
    @Operation(summary = "获取大模型分析结果")
    public ResponseEntity<?> getModelAnalysis(@RequestBody ReportModelRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            ReportModelResponse response = reportService.generateModelAnalysis(request);
            long endTime = System.currentTimeMillis();
            log.info("生成报告耗时: {}ms", (endTime - startTime));

            // 确保返回标准格式
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "操作成功");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("生成报告失败: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "生成报告失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 测试接口，用于验证控制器是否正常工作
     */
    @GetMapping({"/test", "/api-test"})
    @Operation(summary = "测试接口")
    public ApiResponse<String> test() {
        log.info("==== 收到测试请求 ====");
        log.info("当前控制器类: {}", this.getClass().getName());

        try {
            // 测试数据库连接
            log.debug("测试数据库连接...");
            // 添加其他需要测试的内容

            log.info("测试成功");
            String message = "报告服务正常运行 - 时间戳: " + System.currentTimeMillis();
            return ApiResponse.success(message);
        } catch (Exception e) {
            log.error("测试接口出错: {}", e.getMessage(), e);
            return ApiResponse.error(500, "测试接口失败: " + e.getMessage());
        }
    }

    /**
     * 简单诊断接口，用于检查控制器是否能正常响应
     */
    @GetMapping({"/ping", "/simple-ping"})
    @Operation(summary = "诊断接口")
    public ApiResponse<String> ping() {
        log.info("收到ping请求");
        return ApiResponse.success("pong - " + System.currentTimeMillis());
    }

    /**
     * 数据库健康检查
     */
    @GetMapping("/db-check")
    @Operation(summary = "数据库健康检查")
    public ApiResponse<Map<String, Object>> checkDatabase() {
        log.info("执行数据库健康检查");
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询数据库版本
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            result.put("status", "ok");
            result.put("version", dbVersion);
            result.put("message", "数据库连接正常");
            log.info("数据库健康检查通过，版本: {}", dbVersion);
            return ApiResponse.success("数据库连接正常", result);
        } catch (Exception e) {
            log.error("数据库健康检查失败: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            return ApiResponse.error(500, "数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 保存报告
     *
     * @param reportData 报告数据
     * @return 保存结果
     */
    @PostMapping("/save")
    @Operation(summary = "保存报告")
    public ResponseEntity<?> saveReport(@RequestBody Object reportData) {
        try {
            log.info("收到保存报告请求");
            Long reportId = reportService.saveReport(reportData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "报告保存成功");
            result.put("data", Map.of("id", reportId));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("保存报告失败: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "保存报告失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 导出报告为PDF
     *
     * @param reportId 报告ID
     * @return PDF文件
     */
    @GetMapping("/export/pdf/{reportId}")
    @Operation(summary = "导出报告PDF")
    public ResponseEntity<byte[]> exportReportPdf(@PathVariable Long reportId) {
        try {
            log.info("收到导出PDF请求，报告ID: {}", reportId);
            byte[] pdfContent = reportService.exportReportAsPdf(reportId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report_" + reportId + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("导出PDF失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        log.error("控制器异常: {}", e.getMessage(), e);
        return ApiResponse.error(500, "服务异常: " + e.getMessage());
    }
}