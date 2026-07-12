package icu.chatvibe_ai.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 自动生成 API 文档（/swagger-ui.html），配置 JWT Bearer 鉴权
 */
@Configuration
public class SpringDocConfig {

    /**
     * OpenAPI 元信息。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 标题/版本/描述 + JWT Bearer 鉴权方案
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatVibe-AI API")
                        .description("个人 AI 应用后端接口文档")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components().addSecuritySchemes("Bearer",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
