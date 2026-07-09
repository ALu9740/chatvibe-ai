package icu.chatvibe_ai.security;

import icu.chatvibe_ai.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 鉴权过滤器。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 解析 Authorization Bearer 头，校验 JWT 与 Redis 黑名单，写入 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final JwtUtil jwtUtil;
    private final SecurityProperties props;
    private final StringRedisTemplate redis;

    /**
     * 过滤逻辑。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 拦截请求 → 解析 JWT → 检查黑名单 → 写入认证上下文
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param chain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            try {
                if (Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + token))) {
                    log.debug("JWT 已登出黑名单命中");
                } else {
                    Claims claims = jwtUtil.parse(token);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);
                    var auth = new UsernamePasswordAuthenticationToken(
                            username, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.debug("JWT 解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 将 token 加入黑名单（登出时调用）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 以剩余 TTL 写入 Redis 黑名单
     * @param token JWT
     */
    public void blacklist(String token) {
        try {
            Claims claims = jwtUtil.parse(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redis.opsForValue().set(BLACKLIST_PREFIX + token, "1", java.time.Duration.ofMillis(ttl));
            }
        } catch (Exception ignored) {
            // 无效 token 无需黑名单
        }
    }
}
