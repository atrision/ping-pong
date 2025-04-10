package com.misuzu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 乒乓球运动分析系统后端应用
 * 主应用入口类
 */
@SpringBootApplication(scanBasePackages = {"com.misuzu", "com.misuzu.controller", "com.misuzu.service", "com.misuzu.config"})
@RequiredArgsConstructor
@EntityScan("com.misuzu.entity")
@EnableJpaRepositories("com.misuzu.repository")
@Slf4j
@RestController
public class BackApplication {

	private final ApplicationContext applicationContext;

	/**
	 * 应用程序入口
	 * 
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}
	
	/**
	 * 应用启动完成后的初始化工作
	 * 
	 * @return CommandLineRunner实例
	 */
	@Bean
	public CommandLineRunner init() {
		return args -> {
			log.info("===========================================");
			log.info("乒乓球运动分析系统后端启动成功！");
			log.info("===========================================");
			
			// 打印所有注册的处理器映射
			log.info("打印所有注册的处理器映射:");
			try {
				org.springframework.web.servlet.handler.AbstractHandlerMethodMapping methodMapping = 
						applicationContext.getBean("requestMappingHandlerMapping", 
								org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.class);
				methodMapping.getHandlerMethods().forEach((key, value) -> {
					log.info("映射: {} -> {}", key, value);
				});
			} catch (Exception e) {
				log.error("打印处理器映射失败", e);
			}
		};
	}
	
	/**
	 * 测试接口
	 * 
	 * @return 测试响应
	 */
	@GetMapping("/test")
	public String test() {
		return "服务器正常运行";
	}
	
	/**
	 * 健康检查接口
	 * 
	 * @return 健康状态
	 */
	@GetMapping("/ping")
	public String ping() {
		log.info("收到根路径ping请求");
		return "pong";
	}
} 