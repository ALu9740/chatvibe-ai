package icu.chatvibe_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 对应 chat_message 表
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 */
    @TableField("session_id")
    private Long sessionId;

    /** 角色：user|assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 元数据（JSON 字符串） */
    private String metadata;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
