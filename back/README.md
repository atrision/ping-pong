# 乒乓球运动分析系统 - 后端服务

本项目是乒乓球运动分析系统的后端服务，使用Spring Boot框架开发，提供用户认证、轨迹分析、训练报告等功能的API接口。

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

## 快速开始

### 1. 准备数据库

1. 创建MySQL数据库:
   ```sql
   CREATE DATABASE pingpong_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 导入数据库结构:
   ```bash
   mysql -u root -p pingpong_db < web.sql
   ```

### 2. 配置应用

修改 `src/main/resources/application.properties` 文件中的数据库连接信息:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pingpong_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
```

### 3. 构建与运行

```bash
# 编译
mvn clean package

# 运行
java -jar target/back-0.0.1-SNAPSHOT.jar
```

或者使用Maven直接运行:

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080/api 启动

## API接口文档

### 认证相关接口

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/check-username` | GET | 检查用户名是否可用 |
| `/api/auth/check-email` | GET | 检查邮箱是否可用 |

### 公共接口

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/api/public/status` | GET | 获取系统状态 |
| `/api/public/health` | GET | 健康检查 |

## 用户认证

本系统使用JWT (JSON Web Token) 进行用户认证。客户端需要在请求头中添加`Authorization`字段，值为`Bearer {token}`。

示例:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYyMDM5MjU0MCwiZXhwIjoxNjIwNDc4OTQwfQ.3xZw3H7QTN8Yx5PqvJfM1u8hK8kQrYZ5q4XL6qZ6H6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y6Z6Y
```

## 默认账户

系统预设了以下账户用于测试:

1. 管理员账户
   - 用户名: admin
   - 密码: admin123

2. 教练账户
   - 用户名: coach_zhang
   - 密码: coach123

## 开发指南

### 项目结构

```
src/main/java/com/misuzu/
├── config/           # 配置类
├── controller/       # 控制器
├── dto/              # 数据传输对象
├── entity/           # 实体类
├── exception/        # 异常处理
├── repository/       # 数据访问层
├── security/         # 安全相关
├── service/          # 业务逻辑层
│   └── impl/         # 服务实现类
├── util/             # 工具类
└── BackApplication.java  # 应用入口
```

### 添加新功能

1. 创建实体类 (Entity)
2. 创建数据访问接口 (Repository)
3. 创建数据传输对象 (DTO)
4. 创建服务接口和实现 (Service)
5. 创建控制器 (Controller)

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request 