package icu.chatvibe_ai.service;

import icu.chatvibe_ai.dto.AuthDtos;

/**
 * 用户服务接口（面向接口编程，预留企业版扩展）。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 个人版实现硬编码单用户；企业版可改为 DB 多用户实现
 */
public interface IUserService {

    /**
     * 登录校验并签发 JWT。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 校验用户名密码，返回 JWT
     * @param req 登录请求
     * @return 登录响应
     */
    AuthDtos.LoginResponse login(AuthDtos.LoginRequest req);

    /**
     * 注册（预留接口，个人版禁用）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 个人版直接抛 410 禁用异常；企业版可实现 DB 写入
     * @param req 注册请求
     * @return 登录响应
     */
    AuthDtos.LoginResponse register(AuthDtos.RegisterRequest req);

    /**
     * 当前用户信息。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 从 SecurityContext 取
     * @param username 用户名
     * @return 当前用户响应
     */
    AuthDtos.MeResponse me(String username);
}
