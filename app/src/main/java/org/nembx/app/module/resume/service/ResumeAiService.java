package org.nembx.app.module.resume.service;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lian
 */
@Service
@Slf4j
public class ResumeAiService {
    private final ChatClient chatClient;

    private final PromptTemplate systemPromptTemplate;

    private final PromptTemplate userPromptTemplate;

    private final BeanOutputConverter<ResumeAnalysisResponseDTO> outputConverter;

    // 中间DTO用于接收AI响应
    private record ResumeAnalysisResponseDTO(
            int overallScore,
            ScoreDetailDTO scoreDetail,
            String summary,
            List<String> strengths,
            List<SuggestionDTO> suggestions
    ) {}

    private record ScoreDetailDTO(
            int contentScore,
            int structureScore,
            int skillMatchScore,
            int expressionScore,
            int projectScore
    ) {}

    private record SuggestionDTO(
            String category,
            String priority,
            String issue,
            String recommendation
    ) {}


    public ResumeAiService(
            ChatClient.Builder chatClientBuilder,
            @Value("classpath:prompt/resume_system_prompt.st") Resource systemPromptResource,
            @Value("classpath:prompt/resume_user_prompt.st") Resource userPromptResource) throws IOException {
        log.info("初始化AI服务");
        this.systemPromptTemplate = new PromptTemplate(
                        systemPromptResource.getContentAsString(StandardCharsets.UTF_8)
                );
        this.userPromptTemplate = new PromptTemplate(
                userPromptResource.getContentAsString(StandardCharsets.UTF_8)
        );
        this.chatClient = chatClientBuilder.build();
        this.outputConverter = new BeanOutputConverter<>(ResumeAnalysisResponseDTO.class);
    }

    public String analyzeResume(Long resumeId, String resumeText) {
        log.info("开始调用大模型分析简历, 简历ID: {}", resumeId);
//        ResumeAnalysisResponseDTO dto;
        // 加载用户提示词并填充变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("resumeText", resumeText);
        String userPrompt = userPromptTemplate.render(variables);
        try {
            // 使用流式或同步的方式调用 AI
            String aiResponse = this.chatClient.prompt()
                    .system(this.systemPromptTemplate.render())
                    .user(userPrompt)
                    .call()
                    .content();
//                    .entity(outputConverter);
//            log.debug("AI响应解析成功: overallScore={}", dto.overallScore());
//            // 转换为业务对象
//            ResumeAnalysisResponse result = convertToResponse(dto, resumeText);
//            log.info("简历分析完成，总分: {}", result.overallScore());
//
//            return result;
            log.info("AI响应解析成功: {}", aiResponse);
            return aiResponse;
        } catch (Exception e) {
            log.error("调用 AI 接口分析简历失败, 简历ID: {}", resumeId, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 分析失败");
        }
    }
}
