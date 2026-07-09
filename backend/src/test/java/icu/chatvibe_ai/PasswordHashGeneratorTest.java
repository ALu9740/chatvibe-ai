package icu.chatvibe_ai;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGeneratorTest {

    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "chatvibeAlu97";
        String hashedPassword = encoder.encode(plainPassword);
        System.out.println("========================================");
        System.out.println("原始密码: " + plainPassword);
        System.out.println("BCrypt哈希: {bcrypt}" + hashedPassword);
        System.out.println("\n完整配置格式:");
        System.out.println("password: '{bcrypt}" + hashedPassword + "'");
        System.out.println("========================================");

        // 验证测试
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("\n验证测试结果: " + matches);
        System.out.println("========================================");
    }
}