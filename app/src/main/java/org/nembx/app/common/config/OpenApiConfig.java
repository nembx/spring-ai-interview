package org.nembx.app.common.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lian
 */
@Configuration
public class OpenApiConfig {
    private static final String TITLE = "SPRING AI INTERVIEW接口文档";

    private static final String DESCRIPTION = "基于 Spring AI 的智能面试与知识库管理系统，提供简历解析、RAG 对话、知识库管理等接口";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title(TITLE)
                        .version("1.0.0")
                        .description(DESCRIPTION)
                        .contact(new Contact()
                                .name("Lian")));
    }
}
