package icu.chatvibe_ai.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ChatVibe 安全相关配置属性绑定。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 绑定 chatvibe.security.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatvibe.security")
public class SecurityProperties {

    /** JWT 配置 */
    private Jwt jwt = new Jwt();
    /** AES 加密配置 */
    private Aes aes = new Aes();
    /** CORS 配置 */
    private Cors cors = new Cors();
    /** 限流配置 */
    private RateLimit rateLimit = new RateLimit();
    /** 硬编码用户配置 */
    private HardcodedUser hardcodedUser = new HardcodedUser();

    @Data
    public static class Jwt {
        private String secret = "";
        private long expirationMs = 604800000L;
        private String issuer = "chatvibe-ai";
    }

    @Data
    public static class Aes {
        private String key = "";
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of();
    }

    @Data
    public static class RateLimit {
        private int loginPerMinute = 5;
        private int aiPerMinute = 30;
    }

    @Data
    public static class HardcodedUser {
        private String username = "Alu";
        // 真实哈希由 application-local.yml（已 gitignore）或 HASHED_PASSWORD 环境变量注入
        private String password = "";
        private String role = "OWNER";
    }
}
