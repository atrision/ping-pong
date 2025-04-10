-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'user',  -- user, coach, admin
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 登录历史表（仅用于验证）
CREATE TABLE IF NOT EXISTS login_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    device_info TEXT,
    login_status VARCHAR(20)  -- success, failed
);

-- 默认管理员账户（密码: admin123）
INSERT INTO users (username, password, email, phone, role, created_at, updated_at)
SELECT 'admin', '$2a$10$XrjOt2FAiO2SjcQxFPDUxO8tJg8Y5ZFLm6rUaWyKXM07KjLbhqHdi', 'admin@pingpong.com', '13800138000', 'admin', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

-- 默认教练账户（密码: coach123）
INSERT INTO users (username, password, email, phone, role, created_at, updated_at)
SELECT 'coach_zhang', '$2a$10$QTksBBf9aoOc0zy8zN3JI.WVzvzppP76P4W5qoW8aZ5.xUVVX7Wz2', 'coach_zhang@pingpong.com', '13800138001', 'coach', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'coach_zhang'
);

-- 注意：完整数据库结构请使用web.sql脚本手动导入 