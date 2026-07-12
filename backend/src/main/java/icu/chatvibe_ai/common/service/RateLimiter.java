package icu.chatvibe_ai.common.service;

import icu.chatvibe_ai.common.exception.BusinessException;
import icu.chatvibe_ai.common.config.SecurityProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流服务（基于 Bucket4j 本地桶）。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 单用户个人应用使用本地限流；如需分布式可换 bucket4j-redis
 */
@Service
public class RateLimiter {

    private final SecurityProperties props;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * 构造限流器。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 注入安全配置
     * @param props 安全配置
     */
    public RateLimiter(SecurityProperties props) {
        this.props = props;
    }

    /**
     * 检查登录限流。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按 IP 限流，超限抛 429
     * @param ip 客户端 IP
     */
    public void checkLogin(String ip) {
        Bucket bucket = buckets.computeIfAbsent("login:" + ip,
                k -> build(props.getRateLimit().getLoginPerMinute()));
        if (!bucket.tryConsume(1)) {
            throw BusinessException.tooManyRequests("登录尝试过于频繁，请稍后再试");
        }
    }

    /**
     * 检查 AI 接口限流。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按用户名限流
     * @param username 用户名
     */
    public void checkAi(String username) {
        Bucket bucket = buckets.computeIfAbsent("ai:" + username,
                k -> build(props.getRateLimit().getAiPerMinute()));
        if (!bucket.tryConsume(1)) {
            throw BusinessException.tooManyRequests("AI 请求过于频繁，请稍后再试");
        }
    }

    private Bucket build(int perMinute) {
        Bandwidth limit = Bandwidth.classic(perMinute, Refill.intervally(perMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
