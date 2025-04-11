package com.misuzu.controller;

import com.misuzu.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 调试控制器
 * 用于测试和排查各种路径问题
 */
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    /**
     * 路径测试接口
     */
    @GetMapping("/path-test")
    public ApiResponse<Map<String, Object>> pathTest() {
        log.info("收到路径测试请求");

        Map<String, Object> data = new HashMap<>();
        data.put("controller", this.getClass().getName());
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "success");

        return ApiResponse.success(data);
    }

    /**
     * 报告相关调试接口
     */
    @GetMapping("/report-test")
    public ApiResponse<String> reportTest() {
        log.info("收到报告调试请求");
        return ApiResponse.success("报告调试成功 - " + System.currentTimeMillis());
    }
}