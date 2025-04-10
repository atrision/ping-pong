package com.misuzu.repository;

import com.misuzu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 继承JpaRepository以获取基本的CRUD操作方法
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 包含用户的Optional对象，如果不存在返回空Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱地址
     * @return 包含用户的Optional对象，如果不存在返回空Optional
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱地址
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByEmail(String email);
} 