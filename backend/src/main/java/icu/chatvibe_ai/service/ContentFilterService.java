package icu.chatvibe_ai.service;

import icu.chatvibe_ai.common.BusinessException;
import icu.chatvibe_ai.config.ChatvibeProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 内容过滤服务。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 加载敏感词表，对输入/输出进行违规内容拦截，符合个人备案要求
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentFilterService {

    private final ChatvibeProperties props;
    private final ResourceLoader resourceLoader;

    private List<String> blocklist = new ArrayList<>();

    /**
     * 启动时加载敏感词。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 读取 blocklist.txt 到内存
     */
    @PostConstruct
    public void init() {
        try {
            Resource res = resourceLoader.getResource(props.getContentFilter().getBlocklistFile());
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                blocklist = r.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                        .collect(Collectors.toList());
            }
            log.info("内容过滤敏感词加载完成: {} 条", blocklist.size());
        } catch (Exception e) {
            log.warn("敏感词表加载失败，将仅做空过滤: {}", e.getMessage());
        }
    }

    /**
     * 校验输入文本。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 命中敏感词则抛 400
     * @param text 待校验文本
     */
    public void checkInput(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String w : blocklist) {
            if (lower.contains(w.toLowerCase(Locale.ROOT))) {
                throw BusinessException.badRequest("输入包含违规内容，已被过滤");
            }
        }
    }

    /**
     * 校验输出文本（流式分块用）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 命中则替换为安全提示
     * @param chunk 待校验文本
     * @return 过滤后的文本
     */
    public String sanitizeOutput(String chunk) {
        if (chunk == null) {
            return null;
        }
        String result = chunk;
        for (String w : blocklist) {
            if (result.toLowerCase(Locale.ROOT).contains(w.toLowerCase(Locale.ROOT))) {
                result = "[内容已过滤]";
                break;
            }
        }
        return result;
    }
}
