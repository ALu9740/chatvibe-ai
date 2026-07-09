package icu.chatvibe_ai.service;

import icu.chatvibe_ai.common.BusinessException;
import icu.chatvibe_ai.config.SecurityProperties;
import icu.chatvibe_ai.dto.AuthDtos;
import icu.chatvibe_ai.security.JwtUtil;
import icu.chatvibe_ai.util.AesCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 硬编码用户服务实现。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 个人版：仅校验配置中的硬编码单用户；register 禁用；预留企业版扩展
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HardcodedUserService implements IUserService {

    private final SecurityProperties props;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AesCryptoUtil aesCryptoUtil;

    /**
     * 登录校验。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 比对硬编码用户名密码，成功签发 JWT；前端密码已加密，需先解密
     * @param req 登录请求
     * @return LoginResponse 含 token 与 expiresIn（秒）
     */
    @Override
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        SecurityProperties.HardcodedUser u = props.getHardcodedUser();
        if (!u.getUsername().equals(req.username())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 前端传输的密码已加密，先解密
        String decryptedPassword;
        try {
            decryptedPassword = aesCryptoUtil.decrypt(req.password());
        } catch (Exception e) {
            log.error("密码解密失败", e);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 兼容 {noop} 明文与 {bcrypt} 哈希
        String stored = u.getPassword();
        boolean ok;
        if (stored.startsWith("{noop}")) {
            ok = stored.substring(6).equals(decryptedPassword);
        } else if (stored.startsWith("{bcrypt}")) {
            ok = passwordEncoder.matches(decryptedPassword, stored.substring(8));
        } else {
            // 兼容直接 BCrypt 哈希
            ok = passwordEncoder.matches(decryptedPassword, stored);
        }
        if (!ok) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        String token = jwtUtil.sign(u.getUsername(), u.getRole());
        return new AuthDtos.LoginResponse(token, jwtUtil.expirationMs() / 1000);
    }

    /**
     * 注册（禁用）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 个人版禁用公开注册，返回 410；接口保留供企业版实现
     * @param req 注册请求
     * @return 不返回，直接抛异常
     */
    @Override
    public AuthDtos.LoginResponse register(AuthDtos.RegisterRequest req) {
        throw new BusinessException(410, "注册功能已禁用：本系统仅限主理人使用");
    }

    /**
     * 当前用户信息。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 从配置读取硬编码用户角色
     * @param username 用户名
     * @return MeResponse
     */
    @Override
    public AuthDtos.MeResponse me(String username) {
        SecurityProperties.HardcodedUser u = props.getHardcodedUser();
        if (!u.getUsername().equals(username)) {
            throw BusinessException.unauthorized("用户不存在");
        }
        return new AuthDtos.MeResponse(u.getUsername(), u.getRole());
    }
}
