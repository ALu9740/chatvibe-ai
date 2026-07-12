package icu.chatvibe_ai.module.auth.security;

import icu.chatvibe_ai.common.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 基于 jjwt 0.12.x 实现 HS256 签发与校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final SecurityProperties props;
    private SecretKey key;

    /**
     * 初始化签名密钥。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 从配置 secret 派生 HMAC-SHA 密钥
     */
    @PostConstruct
    public void init() {
        byte[] bytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn("JWT secret 长度不足 32 字节，建议在 application-local.yml 中配置更强的密钥");
        }
        this.key = Keys.hmacShaKeyFor(bytes.length >= 32 ? bytes : padTo32(bytes));
    }

    private byte[] padTo32(byte[] src) {
        byte[] out = new byte[32];
        for (int i = 0; i < 32; i++) {
            out[i] = src[i % src.length];
        }
        return out;
    }

    /**
     * 签发 JWT。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 签发包含 username/role 的 token
     * @param username 用户名
     * @param role 角色
     * @return JWT 字符串
     */
    public String sign(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getJwt().getExpirationMs());
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .issuer(props.getJwt().getIssuer())
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 JWT。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 解析 token，失败抛 JwtException
     * @param token JWT 字符串
     * @return Claims
     */
    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取过期时长（毫秒）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 用于前端展示与缓存黑名单 TTL
     * @return 毫秒
     */
    public long expirationMs() {
        return props.getJwt().getExpirationMs();
    }
}
