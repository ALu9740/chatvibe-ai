package icu.chatvibe_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.chatvibe_ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper。
 *
 * @author Alu
 * @date 2026-07-07
 * @description MyBatis-Plus BaseMapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
