package icu.chatvibe_ai.controller;

import icu.chatvibe_ai.common.Result;
import icu.chatvibe_ai.dto.ChatDtos.MessageVo;
import icu.chatvibe_ai.dto.ChatDtos.SessionVo;
import icu.chatvibe_ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 聊天 Controller。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 多模态聊天 / 会话 CRUD / SSE 流式响应
 */
@Tag(name = "AI 聊天")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 多模态聊天（SSE）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description form 提交：prompt + chatId + 可选 files[]，返回 text/event-stream
     * @param username 用户名
     * @param prompt 文本
     * @param chatId 会话 ID（可选）
     * @param files 附件（可选）
     * @return SseEmitter
     */
    @Operation(summary = "多模态聊天（SSE 流）")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@AuthenticationPrincipal String username,
                           @RequestParam("prompt") String prompt,
                           @RequestParam(value = "chatId", required = false) Long chatId,
                           @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return chatService.chat(username, prompt, chatId, files);
    }

    /**
     * 会话列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 仅 chat 类型
     * @param username 用户名
     * @return 会话列表
     */
    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public Result<List<SessionVo>> sessions(@AuthenticationPrincipal String username) {
        return Result.ok(chatService.sessions(username));
    }

    /**
     * 会话消息列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按时间正序
     * @param username 用户名
     * @param id 会话 ID
     * @return 消息列表
     */
    @Operation(summary = "会话消息")
    @GetMapping("/sessions/{id}/messages")
    public Result<List<MessageVo>> messages(@AuthenticationPrincipal String username,
                                            @PathVariable Long id) {
        return Result.ok(chatService.messages(id, username));
    }

    /**
     * 删除会话。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 软删会话 + 硬删消息
     * @param username 用户名
     * @param id 会话 ID
     * @return Result
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{id}")
    public Result<Void> delete(@AuthenticationPrincipal String username,
                               @PathVariable Long id) {
        chatService.deleteSession(id, username);
        return Result.ok();
    }
}
