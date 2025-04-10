package com.misuzu.service.impl;

import com.misuzu.dto.ApiResponse;
import com.misuzu.dto.LoginRequest;
import com.misuzu.dto.LoginResponse;
import com.misuzu.dto.RegisterRequest;
import com.misuzu.entity.User;
import com.misuzu.entity.UserRole;
import com.misuzu.exception.BusinessException;
import com.misuzu.repository.UserRepository;
import com.misuzu.service.AuthService;
import com.misuzu.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 * 实现AuthService接口，处理用户认证相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;
    private final HttpServletRequest request;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应，包含用户信息和JWT令牌
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 参数验证
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空", HttpStatus.BAD_REQUEST);
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空", HttpStatus.BAD_REQUEST);
        }

        // 2. 查找用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.NOT_FOUND));

        try {
            // 3. 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());
            
            // 4. 执行认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 5. 设置认证到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 6. 生成JWT令牌
            String jwtToken = jwtUtil.generateToken(authentication);
            
            // 7. 更新最后登录时间
            user.login();
            userRepository.save(user);
            
            // 8. 记录登录历史
            recordLoginHistory(user.getId(), true);
            
            // 9. 构建并返回登录响应
            return LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(String.valueOf(user.getRole()))
                    .avatarUrl(user.getAvatarUrl())
                    .token(jwtToken)
                    .build();
                    
        } catch (BadCredentialsException e) {
            // 密码错误
            recordLoginHistory(user.getId(), false);
            log.warn("用户 {} 登录失败：密码错误", loginRequest.getUsername());
            throw new BusinessException("用户名或密码错误", HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            // 其他异常
            recordLoginHistory(user.getId(), false);
            log.error("用户 {} 登录失败：{}", loginRequest.getUsername(), e.getMessage());
            throw new BusinessException("登录失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 记录登录历史
     * 
     * @param userId 用户ID
     * @param success 是否登录成功
     */
    private void recordLoginHistory(Integer userId, boolean success) {
        String ipAddress = getClientIpAddress();
        String deviceInfo = request.getHeader("User-Agent");
        String status = success ? "success" : "failed";
        
        String sql = "INSERT INTO login_history (user_id, login_time, ip_address, device_info, login_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try {
            jdbcTemplate.update(sql, userId, LocalDateTime.now(), ipAddress, deviceInfo, status);
        } catch (Exception e) {
            log.error("记录登录历史失败：{}", e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @return IP地址
     */
    private String getClientIpAddress() {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return API响应，包含注册结果
     */
    @Override
    @Transactional
    public ApiResponse<?> register(RegisterRequest registerRequest) {
        // 1. 参数验证
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            return ApiResponse.error(400, "用户名不能为空");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        
        // 2. 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ApiResponse.error(400, "用户名已存在");
        }
        
        // 3. 检查邮箱是否已存在
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            return ApiResponse.error(400, "邮箱已存在");
        }
        
        try {
            // 4. 创建新用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setRole(UserRole.USER); // 直接使用枚举常量，而不是字符串转换
            
            // 5. 保存用户
            userRepository.save(user);
            
            log.info("用户注册成功: {}", user.getUsername());
            
            return ApiResponse.success("注册成功", null);
            
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ApiResponse.error(500, "注册失败：" + e.getMessage());
        }
    }
} 