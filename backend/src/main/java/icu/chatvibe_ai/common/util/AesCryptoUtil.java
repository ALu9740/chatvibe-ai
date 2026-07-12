package icu.chatvibe_ai.common.util;

import icu.chatvibe_ai.common.config.SecurityProperties;
import icu.chatvibe_ai.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加密工具。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 提供敏感字段（如会话元数据、可选配置项）的端到端加密存储
 */
@Slf4j
@Component
public class AesCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12;       // bytes

    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    /**
     * 构造加密器。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 用配置中的 key（Base64 格式）直接解码为 32 字节 AES 密钥
     * @param props 安全配置属性
     */
    public AesCryptoUtil(SecurityProperties props) {
        try {
            // 直接从 Base64 解码密钥（与前端保持一致）
            byte[] raw = Base64.getDecoder().decode(props.getAes().getKey());
            this.keySpec = new SecretKeySpec(raw, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("初始化 AES 密钥失败", e);
        }
    }

    /**
     * 加密明文。
     *
     * @author Alu
     * @date 2026-07-07
     * @description AES-256-GCM 加密，返回 Base64(IV + 密文+Tag)
     * @param plaintext 明文
     * @return Base64 字符串
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new BusinessException(500, "加密失败");
        }
    }

    /**
     * 解密密文。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 解密 encrypt() 产出的 Base64 字符串；若解密失败则按明文返回（兼容旧数据）
     * @param ciphertext Base64 字符串
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length <= IV_LENGTH) {
                // 不是加密格式，按明文返回
                return ciphertext;
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 解密失败说明是旧明文数据，直接返回原文
            log.debug("AES 解密失败，按明文返回: {}", e.getMessage());
            return ciphertext;
        }
    }
}
