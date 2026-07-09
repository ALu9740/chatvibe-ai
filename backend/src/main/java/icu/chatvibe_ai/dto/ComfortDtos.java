package icu.chatvibe_ai.dto;

/**
 * 学会哄人相关 DTO。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 场景列表 / 教练对话请求
 */
public final class ComfortDtos {

    /** 场景列表项 */
    public record ScenarioVo(String key, String label, String desc) {}

    /** 教练对话请求 */
    public record ComfortChatRequest(String scenario, String chatId, String prompt, String context) {}

    private ComfortDtos() {}
}
