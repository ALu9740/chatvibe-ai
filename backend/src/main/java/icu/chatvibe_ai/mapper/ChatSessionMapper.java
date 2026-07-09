package icu.chatvibe_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.chatvibe_ai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper。
 *
 * @author Alu
 * @date 2026-07-07
 * @description MyBatis-Plus BaseMapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
