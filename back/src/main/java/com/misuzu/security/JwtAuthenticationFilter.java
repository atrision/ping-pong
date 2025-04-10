package com.misuzu.security;

import com.misuzu.service.impl.UserDetailsServiceImpl;
import com.misuzu.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT令牌
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.header}")
    private String headerName;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    /**
     * 过滤器的核心方法，在每个请求中执行一次
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException 输入输出异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 从请求中获取JWT令牌
            String jwt = getJwtFromRequest(request);

            // 验证JWT令牌是否存在且有效
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 从JWT令牌中获取用户名
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // 继续过滤器链的处理
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     *
     * @param request 请求对象
     * @return JWT令牌，如果不存在则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从请求头中获取Authorization值
        String bearerToken = request.getHeader(headerName);
        
        // 验证令牌是否存在并以Bearer开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            // 去掉Bearer前缀，返回实际的JWT令牌
            return bearerToken.substring(tokenPrefix.length()).trim();
        }
        return null;
    }
} 