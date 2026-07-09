package icu.chatvibe_ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Spring AI ChatClient 与向量库配置。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 装配多模态聊天 / PDF RAG / 学会哄人 三套 ChatClient 与共享向量库
 */
@Configuration
public class ChatClientConfig {

    /**
     * 共享对话记忆。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 会话级记忆，按 conversationId 隔离
     * @return ChatMemory
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * 简单向量库（基于文件持久化）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 使用 SimpleVectorStore 替代 PgVector，减少外部依赖
     * @param embeddingModel Spring AI 自动注入的通义千问 embedding
     * @return VectorStore
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 默认 AI 聊天 ChatClient。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 多模态聊天，附 Memory Advisor
     * @param builder Spring AI 自动注入的 builder
     * @param memory 共享记忆
     * @return ChatClient
     */
    @Bean("chatClient")
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory memory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    /**
     * PDF RAG ChatClient。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 仅用于 PDF 问答，使用同一 builder 派生
     * @param builder builder
     * @return ChatClient
     */
    @Bean("pdfChatClient")
    public ChatClient pdfChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * 学会哄人 ChatClient。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 情感沟通教练，使用同一 builder 派生
     * @param builder builder
     * @return ChatClient
     */
    @Bean("comfortChatClient")
    public ChatClient comfortChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * 文本分片器。
     *
     * @author Alu
     * @date 2026-07-07
     * @description PDF 解析后按 token 分片
     * @return TokenTextSplitter
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    /**
     * 解析 PDF 资源为文档分片。
     *
     * @author Alu
     * @date 2026-07-07
     * @description Tika 读取 + TokenTextSplitter 分片
     * @param resource PDF 资源
     * @param splitter 分片器
     * @return 文档分片列表
     */
    public static List<Document> splitPdf(Resource resource, TokenTextSplitter splitter) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        return splitter.apply(reader.read());
    }
}
