package icu.chatvibe_ai.module.auth.controller;

import icu.chatvibe_ai.common.result.Result;
import icu.chatvibe_ai.module.auth.dto.AuthDtos;
import icu.chatvibe_ai.module.auth.security.JwtAuthFilter;
import icu.chatvibe_ai.module.auth.service.IUserService;
import icu.chatvibe_ai.common.service.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 鉴权 Controller。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 登录 / 当前用户 / 注册（禁用）/ 登出
 */
@Tag(name = "鉴权")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final RateLimiter rateLimiter;
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * 登录。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 限流 → 校验硬编码用户 → 签发 JWT
     * @param req 登录请求
     * @param request HTTP 请求（取 IP）
     * @return LoginResponse
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req,
                                                HttpServletRequest request) {
        rateLimiter.checkLogin(clientIp(request));
        return Result.ok(userService.login(req));
    }

    /**
     * 当前用户信息。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 从 SecurityContext 取
     * @param username 用户名
     * @return MeResponse
     */
    @Operation(summary = "当前用户")
    @GetMapping("/me")
    public Result<AuthDtos.MeResponse> me(@AuthenticationPrincipal String username) {
        return Result.ok(userService.me(username));
    }

    /**
     * 注册（已禁用）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 个人版禁用，返回 410；接口保留供企业版扩展
     * @param req 注册请求
     * @return 不返回，抛 410 异常
     */
    @Operation(summary = "注册（已禁用）")
    @PostMapping("/register")
    public Result<AuthDtos.LoginResponse> register(@RequestBody AuthDtos.RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    /**
     * 登出。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 将当前 JWT 加入 Redis 黑名单
     * @param auth Authorization 头
     * @return Result
     */
    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            jwtAuthFilter.blacklist(auth.substring(7));
        }
        return Result.ok();
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
