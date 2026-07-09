package icu.chatvibe_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 对应 chat_session 表，统一承载 chat/pdf/comfort 三种类型
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 */
    @TableField("user_id")
    private Long userId;

    /** 类型：chat|pdf|comfort */
    private String type;

    /** 会话标题 */
    private String title;

    /** 学会哄人场景 key（仅 type=comfort） */
    @TableField("scenario_key")
    private String scenarioKey;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
