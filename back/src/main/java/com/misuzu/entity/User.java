package com.misuzu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的users表
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 用户ID，主键，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;
    
    /**
     * 用户名，不能为空，唯一
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码，不能为空，加密存储
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 邮箱，唯一
     */
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * 用户角色：user(普通用户)、coach(教练)、admin(管理员)
     */
//    @Column(name = "role", length = 20)
//    private String role = "user";  // 默认为普通用户
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role = UserRole.USER; // 使用枚举类型
    
    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    /**
     * 创建时间，默认为当前时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 更新时间，默认为当前时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * 更新用户的更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 用户登录更新最后登录时间
     */
    public void login() {
        this.lastLogin = LocalDateTime.now();
    }
} 