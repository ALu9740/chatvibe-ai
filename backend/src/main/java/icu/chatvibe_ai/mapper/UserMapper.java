package icu.chatvibe_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.chatvibe_ai.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * @author Alu
 * @date 2026-07-07
 * @description MyBatis-Plus BaseMapper，预留企业版扩展
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
