package icu.chatvibe_ai.security;

import icu.chatvibe_ai.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 硬编码用户详情服务。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 个人开发者备案场景下从配置加载硬编码单用户；预留扩展点供企业版接 DB
 */
@Service
@RequiredArgsConstructor
public class HardcodedUserDetailsService implements UserDetailsService {

    private final SecurityProperties props;

    /**
     * 按用户名加载用户。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 仅匹配配置中的硬编码用户名
     * @param username 用户名
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityProperties.HardcodedUser u = props.getHardcodedUser();
        if (!u.getUsername().equals(username)) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new User(
                u.getUsername(),
                u.getPassword(),  // 形如 {noop}xxx 或 {bcrypt}$2a$...
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
        );
    }
}
