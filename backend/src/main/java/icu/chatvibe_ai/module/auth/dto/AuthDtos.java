package icu.chatvibe_ai.module.auth.dto;

/**
 * 鉴权相关 DTO。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 登录/注册/当前用户响应
 */
public final class AuthDtos {

    /** 登录请求 */
    public record LoginRequest(String username, String password) {}

    /** 登录响应 */
    public record LoginResponse(String token, long expiresIn) {}

    /** 注册请求（预留，当前禁用） */
    public record RegisterRequest(String username, String password) {}

    /** 当前用户响应 */
    public record MeResponse(String username, String role) {}

    private AuthDtos() {}
}
