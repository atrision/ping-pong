package com.misuzu.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
@Slf4j
public class JwtUtil {
    
    /**
     * JWT密钥，从配置文件中读取
     */
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    /**
     * JWT有效期（毫秒），从配置文件中读取
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    /**
     * JWT签名算法，从配置文件中读取，默认为HS512
     */
    @Value("${jwt.algorithm:HS512}")
    private String jwtAlgorithm;
    
    /**
     * 生成JWT密钥
     *
     * @return 密钥对象
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        
        // 检查密钥长度是否满足要求
        int keyLength = keyBytes.length * 8; // 转换为位数
        SignatureAlgorithm algorithm = getSignatureAlgorithm();
        
        if (algorithm == SignatureAlgorithm.HS256 && keyLength < 256) {
            log.warn("密钥长度({} bits)不足，HS256算法建议至少256位", keyLength);
        } else if (algorithm == SignatureAlgorithm.HS384 && keyLength < 384) {
            log.warn("密钥长度({} bits)不足，HS384算法建议至少384位", keyLength);
        } else if (algorithm == SignatureAlgorithm.HS512 && keyLength < 512) {
            log.warn("密钥长度({} bits)不足，HS512算法建议至少512位", keyLength);
        } else {
            log.info("JWT密钥长度充足：{} bits，算法：{}", keyLength, algorithm);
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 获取签名算法
     *
     * @return SignatureAlgorithm对象
     */
    private SignatureAlgorithm getSignatureAlgorithm() {
        switch (jwtAlgorithm.toUpperCase()) {
            case "HS256":
                return SignatureAlgorithm.HS256;
            case "HS384":
                return SignatureAlgorithm.HS384;
            case "HS512":
            default:
                return SignatureAlgorithm.HS512;
        }
    }
    
    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中获取过期日期
     *
     * @param token JWT令牌
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从令牌中获取指定声明
     *
     * @param token JWT令牌
     * @param claimsResolver 声明解析函数
     * @param <T> 声明类型
     * @return 解析后的声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从令牌中获取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 如果过期返回true，否则返回false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 生成令牌
     *
     * @param authentication 认证对象
     * @return JWT令牌
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername());
    }
    
    /**
     * 根据用户名生成令牌
     *
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }
    
    /**
     * 生成令牌的内部方法
     *
     * @param claims 声明
     * @param subject 主题（用户名）
     * @return JWT令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), getSignatureAlgorithm())
                .compact();
    }
    
    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 如果有效返回true，否则返回false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 如果有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
} 