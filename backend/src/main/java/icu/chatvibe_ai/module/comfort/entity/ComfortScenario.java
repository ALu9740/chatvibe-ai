package icu.chatvibe_ai.module.comfort.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学会哄人场景实体。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 对应 comfort_scenario 表，由 data.sql 初始化内置场景
 */
@Data
@TableName("comfort_scenario")
public class ComfortScenario {

    /** 场景 key（主键） */
    @TableId("`key`")
    private String key;

    /** 场景名称 */
    private String label;

    /** 场景描述 */
    private String description;

    /** 系统提示词 */
    @TableField("system_prompt")
    private String systemPrompt;

    /** 排序 */
    private Integer sort;
}
