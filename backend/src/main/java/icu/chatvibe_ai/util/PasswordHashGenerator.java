package icu.chatvibe_ai.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码哈希生成工具（仅用于生成BCrypt哈希，不参与运行时逻辑）
 *
 * @author Alu
 * @date 2026-07-07
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String plainPassword = "chatvibeAlu97";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("原始密码: " + plainPassword);
        System.out.println("BCrypt哈希: {bcrypt}" + hashedPassword);
        System.out.println("\n完整配置格式:");
        System.out.println("password: '{bcrypt}" + hashedPassword + "'");

        // 验证测试
        System.out.println("\n验证测试:");
        System.out.println("验证结果: " + BCrypt.checkpw(plainPassword, hashedPassword));
    }
}