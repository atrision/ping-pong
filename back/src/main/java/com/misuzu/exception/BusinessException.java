package com.misuzu.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常类
 * 用于表示业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;
    
    /**
     * HTTP状态码
     */
    private final HttpStatus status;

    /**
     * 创建业务异常
     * 
     * @param message 错误消息
     * @param code 错误码
     * @param status HTTP状态码
     */
    public BusinessException(String message, int code, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /**
     * 创建业务异常，使用400错误码
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(message, 400, HttpStatus.BAD_REQUEST);
    }

    /**
     * 创建业务异常
     * 
     * @param message 错误消息
     * @param status HTTP状态码
     */
    public BusinessException(String message, HttpStatus status) {
        this(message, status.value(), status);
    }
} 