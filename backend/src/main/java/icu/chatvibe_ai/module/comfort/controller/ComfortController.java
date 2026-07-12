package icu.chatvibe_ai.module.comfort.controller;

import icu.chatvibe_ai.common.result.Result;
import icu.chatvibe_ai.module.comfort.dto.ComfortDtos;
import icu.chatvibe_ai.module.comfort.service.ComfortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 学会哄人 Controller。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 场景列表 / 教练对话（SSE，含评分元信息）
 */
@Tag(name = "学会哄人")
@RestController
@RequestMapping("/api/comfort")
@RequiredArgsConstructor
public class ComfortController {

    private final ComfortService comfortService;

    /**
     * 场景列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按排序返回 5 个内置场景
     * @return 场景列表
     */
    @Operation(summary = "场景列表")
    @GetMapping("/scenarios")
    public Result<List<ComfortDtos.ScenarioVo>> scenarios() {
        return Result.ok(comfortService.scenarios());
    }

    /**
     * 教练对话（SSE）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 流式输出当事人反应 + 教练点评；完成前发送 {score, tip} 元信息
     * @param username 用户名
     * @param req 请求体
     * @return SseEmitter
     */
    @Operation(summary = "教练对话（SSE 流）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@AuthenticationPrincipal String username,
                           @RequestBody ComfortDtos.ComfortChatRequest req) {
        return comfortService.chat(username, req);
    }
}
