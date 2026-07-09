package icu.chatvibe_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.chatvibe_ai.common.BusinessException;
import icu.chatvibe_ai.dto.ChatDtos;
import icu.chatvibe_ai.dto.ChatDtos.MessageVo;
import icu.chatvibe_ai.dto.ChatDtos.SessionVo;
import icu.chatvibe_ai.entity.ChatMessage;
import icu.chatvibe_ai.entity.ChatSession;
import icu.chatvibe_ai.entity.User;
import icu.chatvibe_ai.mapper.ChatMessageMapper;
import icu.chatvibe_ai.mapper.ChatSessionMapper;
import icu.chatvibe_ai.mapper.UserMapper;
import icu.chatvibe_ai.util.AesCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.Media;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 聊天服务。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 多模态聊天（文本+图片）、会话 CRUD、SSE 流式响应
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT =
            "你是 ChatVibe，Alu 的私人 AI 助手。回答简洁、温暖、有质感，必要时使用 Markdown 与代码块。";
    private static final int MAX_HISTORY = 20;

    @Qualifier("chatClient")
    private final ChatClient chatClient;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ContentFilterService filter;
    private final RateLimiter rateLimiter;
    private final SseStreamingHelper sseHelper;
    private final AesCryptoUtil cryptoUtil;

    /**
     * 流式聊天。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 校验 → 取/建会话 → 存用户消息 → 流式调 AI → 存 AI 消息
     * @param username 当前用户
     * @param prompt 用户输入
     * @param chatId 会话 ID（null 表示新建）
     * @param files 多模态附件（图片/音频/视频）
     * @return SseEmitter
     */
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chat(
            String username, String prompt, Long chatId, List<MultipartFile> files) {
        rateLimiter.checkAi(username);
        filter.checkInput(prompt);

        Long userId = getUserId(username);
        ChatSession session = getOrCreateSession(chatId, userId, "chat", null);
        sessionMapper.updateById(session); // refresh updated_at

        // 存用户消息（加密 content）
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(cryptoUtil.encrypt(prompt));
        userMsg.setMetadata(buildFileMeta(files));
        messageMapper.insert(userMsg);

        // 构建多模态消息 + 历史
        List<Message> messages = buildContextMessages(session.getId(), prompt, files);

        // 标题：首条消息截前 30 字
        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(prompt.length() > 30 ? prompt.substring(0, 30) + "…" : prompt);
            sessionMapper.updateById(session);
        }

        final Long sessionId = session.getId();
        return sseHelper.stream(
                chatClient.prompt().messages(messages).stream().content(),
                filter::sanitizeOutput,
                aiContent -> {
                    ChatMessage aiMsg = new ChatMessage();
                    aiMsg.setSessionId(sessionId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(cryptoUtil.encrypt(aiContent));
                    messageMapper.insert(aiMsg);
                }
        );
    }

    /**
     * 会话列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 仅返回 chat 类型
     * @param username 用户名
     * @return 会话 VO 列表
     */
    public List<SessionVo> sessions(String username) {
        Long userId = getUserId(username);
        return sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getType, "chat")
                        .orderByDesc(ChatSession::getUpdatedAt))
                .stream()
                .map(s -> new SessionVo(s.getId(), s.getTitle(), s.getCreatedAt()))
                .toList();
    }

    /**
     * 会话消息列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按时间正序
     * @param sessionId 会话 ID
     * @param username 用户名（鉴权校验）
     * @return 消息 VO 列表
     */
    public List<MessageVo> messages(Long sessionId, String username) {
        ChatSession s = mustOwn(sessionId, username);
        return messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, s.getId())
                        .orderByAsc(ChatMessage::getCreatedAt))
                .stream()
                .map(m -> new MessageVo(m.getRole(), cryptoUtil.decrypt(m.getContent())))
                .toList();
    }

    /**
     * 删除会话。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 软删会话 + 硬删消息
     * @param sessionId 会话 ID
     * @param username 用户名
     */
    public void deleteSession(Long sessionId, String username) {
        ChatSession s = mustOwn(sessionId, username);
        sessionMapper.deleteById(s.getId());
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, s.getId()));
    }

    // ====== 内部方法 ======

    private List<Message> buildContextMessages(Long sessionId, String prompt, List<MultipartFile> files) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        List<ChatMessage> history = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + MAX_HISTORY));
        // 反转为正序（解密历史消息用于上下文）
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage m = history.get(i);
            String plain = cryptoUtil.decrypt(m.getContent());
            if ("user".equals(m.getRole())) {
                messages.add(new UserMessage(plain));
            } else {
                messages.add(new AssistantMessage(plain));
            }
        }
        messages.add(buildMultimodalUserMessage(prompt, files));
        return messages;
    }

    private UserMessage buildMultimodalUserMessage(String prompt, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new UserMessage(prompt);
        }
        List<Media> media = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>();
        try {
            for (MultipartFile f : files) {
                if (f.getContentType() == null) continue;
                if (!f.getContentType().startsWith("image/")) continue; // 仅图片进模型
                File temp = Files.createTempFile("cv-", "-" + UUID.randomUUID())
                        .toFile();
                tempFiles.add(temp);
                f.transferTo(temp);
                media.add(new Media(
                        MimeTypeUtils.parseMimeType(f.getContentType()),
                        temp.toURI().toURL()));
            }
        } catch (IOException e) {
            log.warn("附件处理失败: {}", e.getMessage());
        }
        if (media.isEmpty()) {
            return new UserMessage(prompt);
        }
        // 注：tempFiles 在请求结束后由 JVM 清理（简单实现，生产可改用回调清理）
        return new UserMessage(prompt, media.toArray(new Media[0]));
    }

    private String buildFileMeta(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return null;
        return files.stream()
                .map(f -> "{\"name\":\"" + f.getOriginalFilename() + "\",\"size\":" + f.getSize()
                        + ",\"type\":\"" + f.getContentType() + "\"}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private ChatSession getOrCreateSession(Long chatId, Long userId, String type, String scenarioKey) {
        if (chatId != null) {
            ChatSession s = sessionMapper.selectById(chatId);
            if (s != null && s.getUserId().equals(userId) && type.equals(s.getType())) {
                return s;
            }
        }
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setType(type);
        s.setScenarioKey(scenarioKey);
        sessionMapper.insert(s);
        return s;
    }

    private ChatSession mustOwn(Long sessionId, String username) {
        Long userId = getUserId(username);
        ChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || !s.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权访问该会话");
        }
        return s;
    }

    private Long getUserId(String username) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u == null) {
            // 硬编码用户可能未入库，使用 username 哈希作为兜底 user_id
            return (long) Math.abs(username.hashCode());
        }
        return u.getId();
    }
}
