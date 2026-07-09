package icu.chatvibe_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.chatvibe_ai.common.BusinessException;
import icu.chatvibe_ai.dto.ComfortDtos;
import icu.chatvibe_ai.entity.ChatMessage;
import icu.chatvibe_ai.entity.ChatSession;
import icu.chatvibe_ai.entity.ComfortScenario;
import icu.chatvibe_ai.entity.User;
import icu.chatvibe_ai.mapper.ChatMessageMapper;
import icu.chatvibe_ai.mapper.ChatSessionMapper;
import icu.chatvibe_ai.mapper.ComfortScenarioMapper;
import icu.chatvibe_ai.mapper.UserMapper;
import icu.chatvibe_ai.util.AesCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学会哄人服务。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 情感沟通教练：场景选择 → AI 扮演当事人 + 教练点评 → 提取评分元信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComfortService {

    private static final int MAX_HISTORY = 10;
    private static final Pattern SCORE_PATTERN =
            Pattern.compile("共情评分\\s*(\\d+)\\s*/\\s*10");
    private static final Pattern TIP_PATTERN =
            Pattern.compile("建议[：:]\\s*([^\\n｜|]+)");

    @Qualifier("comfortChatClient")
    private final ChatClient comfortChatClient;
    private final ComfortScenarioMapper scenarioMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ContentFilterService filter;
    private final RateLimiter rateLimiter;
    private final SseStreamingHelper sseHelper;
    private final AesCryptoUtil cryptoUtil;

    /**
     * 场景列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 按排序返回全部场景
     * @return 场景 VO 列表
     */
    public List<ComfortDtos.ScenarioVo> scenarios() {
        return scenarioMapper.selectList(new LambdaQueryWrapper<ComfortScenario>()
                        .orderByAsc(ComfortScenario::getSort))
                .stream()
                .map(s -> new ComfortDtos.ScenarioVo(s.getKey(), s.getLabel(), s.getDescription()))
                .toList();
    }

    /**
     * 教练对话。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 校验场景 → 取/建会话 → 存用户消息 → 流式 AI → 完成时提取评分元信息并推送 → 存 AI 消息
     * @param username 用户名
     * @param req 请求体
     * @return SseEmitter
     */
    public SseEmitter chat(String username, ComfortDtos.ComfortChatRequest req) {
        rateLimiter.checkAi(username);
        filter.checkInput(req.prompt());

        ComfortScenario scenario = scenarioMapper.selectById(req.scenario());
        if (scenario == null) {
            throw BusinessException.badRequest("未知场景: " + req.scenario());
        }

        Long userId = getUserId(username);
        Long chatId = parseChatId(req.chatId());
        ChatSession session = getOrCreateComfortSession(chatId, userId, scenario.getKey());

        // 存用户消息（加密 content）
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(cryptoUtil.encrypt(req.prompt()));
        messageMapper.insert(userMsg);

        // 构建 prompt：系统 + 历史 + 当前（含 context）
        List<Message> messages = buildContextMessages(session.getId(), scenario, req);

        final Long sessionId = session.getId();
        return sseHelper.streamWithMeta(
                comfortChatClient.prompt().messages(messages).stream().content(),
                filter::sanitizeOutput,
                (aiContent, emitter) -> {
                    // 存 AI 消息（加密 content）
                    ChatMessage aiMsg = new ChatMessage();
                    aiMsg.setSessionId(sessionId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(cryptoUtil.encrypt(aiContent));
                    messageMapper.insert(aiMsg);

                    // 提取评分与建议，发送 meta 事件
                    Integer score = extractScore(aiContent);
                    String tip = extractTip(aiContent);
                    if (score != null) {
                        String meta = "{\"score\":" + score + ",\"tip\":\"" + escape(tip) + "\"}";
                        try {
                            emitter.send(SseEmitter.event().data(meta));
                        } catch (Exception e) {
                            log.debug("meta 发送失败: {}", e.getMessage());
                        }
                    }
                }
        );
    }

    // ====== 内部方法 ======

    private List<Message> buildContextMessages(Long sessionId, ComfortScenario scenario,
                                               ComfortDtos.ComfortChatRequest req) {
        List<Message> messages = new ArrayList<>();
        String systemText = scenario.getSystemPrompt();
        if (req.context() != null && !req.context().isBlank()) {
            systemText += "\n\n【用户补充的情境】" + req.context();
        }
        messages.add(new SystemMessage(systemText));

        List<ChatMessage> history = messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + MAX_HISTORY));
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage m = history.get(i);
            String plain = cryptoUtil.decrypt(m.getContent());
            if ("user".equals(m.getRole())) {
                messages.add(new UserMessage(plain));
            } else {
                messages.add(new AssistantMessage(plain));
            }
        }
        messages.add(new UserMessage(req.prompt()));
        return messages;
    }

    private Integer extractScore(String content) {
        if (content == null) return null;
        Matcher m = SCORE_PATTERN.matcher(content);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String extractTip(String content) {
        if (content == null) return "";
        Matcher m = TIP_PATTERN.matcher(content);
        return m.find() ? m.group(1).trim() : "";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }

    private Long parseChatId(String chatId) {
        if (chatId == null || chatId.isBlank() || "new".equals(chatId)) {
            return null;
        }
        try {
            return Long.parseLong(chatId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ChatSession getOrCreateComfortSession(Long chatId, Long userId, String scenarioKey) {
        if (chatId != null) {
            ChatSession s = sessionMapper.selectById(chatId);
            if (s != null && s.getUserId().equals(userId) && "comfort".equals(s.getType())) {
                return s;
            }
        }
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setType("comfort");
        s.setScenarioKey(scenarioKey);
        s.setTitle("学会哄人 · " + scenarioKey);
        sessionMapper.insert(s);
        return s;
    }

    private Long getUserId(String username) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u == null) {
            return (long) Math.abs(username.hashCode());
        }
        return u.getId();
    }
}
