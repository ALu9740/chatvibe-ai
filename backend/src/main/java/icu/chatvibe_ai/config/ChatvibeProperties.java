package icu.chatvibe_ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ChatVibe 业务配置绑定。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 绑定 chatvibe.pdf / chatvibe.vector / chatvibe.content-filter
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatvibe")
public class ChatvibeProperties {

    /** PDF 存储配置 */
    private Pdf pdf = new Pdf();
    /** 向量库配置 */
    private Vector vector = new Vector();
    /** 内容过滤配置 */
    private ContentFilter contentFilter = new ContentFilter();

    @Data
    public static class Pdf {
        private String storageDir = "./data/pdf";
    }

    @Data
    public static class Vector {
        private String storeFile = "./data/vector-store.json";
    }

    @Data
    public static class ContentFilter {
        private String blocklistFile = "classpath:blocklist.txt";
    }
}
