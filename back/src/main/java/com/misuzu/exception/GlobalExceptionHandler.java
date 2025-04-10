package com.misuzu.exception;

import com.misuzu.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的各种异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     * 
     * @param ex 方法参数不合法异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("参数校验失败: {}", errors);
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(400, "参数验证失败");
        response.setData(errors);
        return response;
    }

    /**
     * 处理认证异常
     * 
     * @param ex 认证异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleAuthenticationException(Exception ex) {
        log.warn("认证失败: {}", ex.getMessage());
        
        return ApiResponse.error(401, "用户名或密码错误");
    }

    /**
     * 处理数据完整性异常（例如唯一约束违反）
     *
     * @param ex 数据完整性异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("数据完整性异常: {}", ex.getMessage());
        
        String message = "操作失败: 数据约束冲突";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("username")) {
                message = "用户名已存在";
            } else if (ex.getMessage().contains("email")) {
                message = "邮箱已存在";
            }
        }
        
        return ApiResponse.error(409, message);
    }

    /**
     * 处理自定义业务异常
     * 
     * @param ex 业务异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException ex) {
        log.error("业务异常: {}", ex.getMessage());
        
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理未捕获的异常
     * 
     * @param ex 异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleGlobalException(Exception ex) {
        log.error("未捕获的异常", ex);
        
        return ApiResponse.error(500, "服务器内部错误，请稍后再试: " + ex.getMessage());
    }
} 