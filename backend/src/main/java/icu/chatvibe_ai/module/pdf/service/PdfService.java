package icu.chatvibe_ai.module.pdf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.chatvibe_ai.common.exception.BusinessException;
import icu.chatvibe_ai.common.config.ChatClientConfig;
import icu.chatvibe_ai.common.config.ChatvibeProperties;
import icu.chatvibe_ai.common.service.ContentFilterService;
import icu.chatvibe_ai.common.service.RateLimiter;
import icu.chatvibe_ai.common.service.SseStreamingHelper;
import icu.chatvibe_ai.module.pdf.dto.PdfDtos;
import icu.chatvibe_ai.module.chat.entity.ChatMessage;
import icu.chatvibe_ai.module.chat.entity.ChatSession;
import icu.chatvibe_ai.module.pdf.entity.PdfFile;
import icu.chatvibe_ai.module.chat.mapper.ChatMessageMapper;
import icu.chatvibe_ai.module.chat.mapper.ChatSessionMapper;
import icu.chatvibe_ai.module.pdf.mapper.PdfFileMapper;
import icu.chatvibe_ai.module.auth.mapper.UserMapper;
import icu.chatvibe_ai.common.util.AesCryptoUtil;
import icu.chatvibe_ai.module.auth.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ChatPDF 服务。
 *
 * @author Alu
 * @date 2026-07-07
 * @description PDF 上传解析向量化 + RAG 流式问答 + 文件下载 + 会话列表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final int TOP_K = 5;
    private static final String SYSTEM_PROMPT =
            "你是 ChatVibe 的 PDF 阅读助手。仅基于提供的文档片段回答用户问题，" +
                    "若文档未涉及请明确说明。回答使用中文 Markdown。";

    @Qualifier("pdfChatClient")
    private final ChatClient pdfChatClient;
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final PdfFileMapper pdfFileMapper;
    private final UserMapper userMapper;
    private final ChatvibeProperties props;
    private final ContentFilterService filter;
    private final RateLimiter rateLimiter;
    private final SseStreamingHelper sseHelper;
    private final AesCryptoUtil cryptoUtil;

    private Path storageDir;

    /**
     * 初始化存储目录。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 启动时创建 PDF 存储目录
     */
    @PostConstruct
    public void init() throws IOException {
        storageDir = Paths.get(props.getPdf().getStorageDir()).toAbsolutePath();
        Files.createDirectories(storageDir);
    }

    /**
     * 上传 PDF。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 保存文件 → 创建会话 → Tika 解析 → 分片 → 向量化入库
     * @param username 用户名
     * @param chatId 会话 ID（可为 null）
     * @param file PDF 文件
     * @return 上传响应
     */
    public PdfDtos.PdfUploadResponse upload(String username, Long chatId, MultipartFile file) {
        rateLimiter.checkAi(username);
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("文件为空");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.toLowerCase().endsWith(".pdf")) {
            throw BusinessException.badRequest("仅支持 PDF 文件");
        }

        Long userId = getUserId(username);
        ChatSession session = getOrCreatePdfSession(chatId, userId);

        String storedName = session.getId() + "-" + System.currentTimeMillis() + ".pdf";
        Path dest = storageDir.resolve(storedName);
        try {
            file.transferTo(dest.toFile());
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }

        // 记录 pdf_file
        PdfFile pdf = new PdfFile();
        pdf.setSessionId(session.getId());
        pdf.setStoredName(storedName);
        pdf.setOriginalName(original);
        pdf.setSize(file.getSize());
        pdfFileMapper.insert(pdf);

        // 解析 + 向量化
        try {
            List<Document> chunks = ChatClientConfig.splitPdf(new UrlResource(dest.toUri().toURL()), tokenTextSplitter);
            // 给每个分片加 sessionId 元数据，便于 RAG 过滤
            chunks.forEach(d -> d.getMetadata().put("sessionId", session.getId().toString()));
            vectorStore.add(chunks);
            session.setTitle(original);
            sessionMapper.updateById(session);
        } catch (Exception e) {
            log.error("PDF 向量化失败", e);
            throw new BusinessException(500, "PDF 解析或向量化失败：" + e.getMessage());
        }

        return new PdfDtos.PdfUploadResponse(true, original, session.getId());
    }

    /**
     * RAG 问答。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 检索相关分片 → 拼 prompt → 流式响应 → 存消息
     * @param username 用户名
     * @param prompt 用户问题
     * @param chatId 会话 ID
     * @return SseEmitter
     */
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chat(
            String username, String prompt, Long chatId) {
        rateLimiter.checkAi(username);
        filter.checkInput(prompt);

        ChatSession session = mustOwn(chatId, username);

        // 存用户消息（加密 content）
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(cryptoUtil.encrypt(prompt));
        messageMapper.insert(userMsg);

        // 检索
        String filterExpr = "sessionId == '" + session.getId() + "'";
        List<Document> docs;
        try {
            docs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(prompt)
                            .topK(TOP_K)
                            .filterExpression(filterExpr)
                            .build());
        } catch (Exception e) {
            log.warn("向量检索失败，退化为无上下文: {}", e.getMessage());
            docs = List.of();
        }
        String context = docs.stream().map(Document::getText)
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));
        String fullPrompt = "文档片段：\n" + context + "\n\n用户问题：" + prompt;

        final Long sessionId = session.getId();
        return sseHelper.stream(
                pdfChatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(fullPrompt)
                        .stream()
                        .content(),
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
     * 下载 PDF 文件。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 返回存储路径上的 File 对象
     * @param username 用户名
     * @param chatId 会话 ID
     * @return File
     */
    public File downloadFile(String username, Long chatId) {
        ChatSession session = mustOwn(chatId, username);
        PdfFile pdf = pdfFileMapper.selectOne(new LambdaQueryWrapper<PdfFile>()
                .eq(PdfFile::getSessionId, session.getId()));
        if (pdf == null) {
            throw BusinessException.badRequest("该会话未上传 PDF");
        }
        File f = storageDir.resolve(pdf.getStoredName()).toFile();
        if (!f.exists()) {
            throw new BusinessException(404, "文件不存在");
        }
        return f;
    }

    /**
     * PDF 会话列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 返回 type=pdf 的会话及其文件名
     * @param username 用户名
     * @return 列表
     */
    public List<PdfDtos.PdfSessionVo> sessions(String username) {
        Long userId = getUserId(username);
        List<ChatSession> ss = sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getType, "pdf")
                .orderByDesc(ChatSession::getUpdatedAt));
        return ss.stream().map(s -> {
            PdfFile pdf = pdfFileMapper.selectOne(new LambdaQueryWrapper<PdfFile>()
                    .eq(PdfFile::getSessionId, s.getId()));
            String name = pdf != null ? pdf.getOriginalName() : (s.getTitle() != null ? s.getTitle() : "未命名");
            return new PdfDtos.PdfSessionVo(s.getId(), name);
        }).toList();
    }

    // ====== 内部方法 ======

    private ChatSession getOrCreatePdfSession(Long chatId, Long userId) {
        if (chatId != null) {
            ChatSession s = sessionMapper.selectById(chatId);
            if (s != null && s.getUserId().equals(userId) && "pdf".equals(s.getType())) {
                return s;
            }
        }
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setType("pdf");
        sessionMapper.insert(s);
        return s;
    }

    private ChatSession mustOwn(Long chatId, String username) {
        Long userId = getUserId(username);
        ChatSession s = sessionMapper.selectById(chatId);
        if (s == null || !s.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权访问该会话");
        }
        return s;
    }

    private Long getUserId(String username) {
        var u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (u == null) {
            return (long) Math.abs(username.hashCode());
        }
        return u.getId();
    }
}
