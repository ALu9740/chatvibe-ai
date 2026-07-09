package icu.chatvibe_ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChatVibe-AI 应用入口类。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 启动 Spring Boot 主程序；扫描 MyBatis-Plus mapper 包
 */
@SpringBootApplication
@MapperScan("icu.chatvibe_ai.mapper")
public class ChatVibeAiApplication {

    /**
     * 主入口方法。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 启动 Spring 容器
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用 JDK 系统 DNS 解析器，避免 Reactor Netty 自带 DNS 解析器在内网/VPN 环境下超时
        System.setProperty("reactor.netty.useDefaultDnsResolver", "true");
        SpringApplication.run(ChatVibeAiApplication.class, args);
    }
}
