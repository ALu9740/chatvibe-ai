package icu.chatvibe_ai.dto;

import java.time.LocalDateTime;

/**
 * 聊天相关 DTO。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 会话列表 / 消息列表 VO
 */
public final class ChatDtos {

    /** 会话列表项 */
    public record SessionVo(Long id, String title, LocalDateTime createdAt) {}

    /** 消息列表项 */
    public record MessageVo(String role, String content) {}

    private ChatDtos() {}
}
