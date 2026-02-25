package org.nembx.app.module.resume.service;


import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.resume.enity.ResumeAnalysis;
import org.nembx.app.module.resume.enity.record.ResumeAnalysisResponse;
import org.nembx.app.module.resume.enity.record.ResumeAnalysisResponse.Suggestion;
import org.nembx.app.module.resume.enity.record.ResumeAnalysisResponse.ScoreDetail;
import org.nembx.app.module.resume.enity.record.ResumeAnalysisResponseDTO;
import org.nembx.app.module.resume.repository.ResumeAnalysisRepository;
import org.nembx.app.module.resume.utils.JsonUtils;
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

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    private final BeanOutputConverter<ResumeAnalysisResponseDTO> outputConverter;


    public ResumeAiService(
            ChatClient.Builder chatClientBuilder,
            @Value("classpath:prompt/resume_system_prompt.st") Resource systemPromptResource,
            @Value("classpath:prompt/resume_user_prompt.st") Resource userPromptResource,
            ResumeAnalysisRepository resumeAnalysisRepository) throws IOException {
        log.info("初始化AI服务");
        this.systemPromptTemplate = new PromptTemplate(
                systemPromptResource.getContentAsString(StandardCharsets.UTF_8)
        );
        this.userPromptTemplate = new PromptTemplate(
                userPromptResource.getContentAsString(StandardCharsets.UTF_8)
        );
        this.chatClient = chatClientBuilder.build();
        this.outputConverter = new BeanOutputConverter<>(ResumeAnalysisResponseDTO.class);
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    public ResumeAnalysisResponse analyzeResume(Long resumeId, String resumeText) {
        log.info("开始调用大模型分析简历, 简历ID: {}", resumeId);
        ResumeAnalysisResponseDTO dto;
        // 加载用户提示词并填充变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("resumeText", resumeText);
        String userPrompt = userPromptTemplate.render(variables);
        try {
            // 使用流式或同步的方式调用 AI
            dto = this.chatClient.prompt()
                    .system(this.systemPromptTemplate.render())
                    .user(userPrompt)
                    .call()
                    .entity(outputConverter);
            if (dto == null){
                log.error("AI 响应解析失败");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 响应解析失败");
            }
            log.debug("AI响应解析成功: overallScore={}", dto.overallScore());
            // 转换为业务对象
            ResumeAnalysisResponse result = convertToResponse(dto, resumeText);
            log.info("简历分析完成，总分: {}", result.overallScore());

            ResumeAnalysis resumeAnalysis = convertToEntity(resumeId, result);
            resumeAnalysisRepository.save(resumeAnalysis);
            log.info("保存简历分析结果到数据库");

            log.info("AI响应解析成功: {}", result);
            return result;
        } catch (Exception e) {
            log.error("调用 AI 接口分析简历失败, 简历ID: {}", resumeId, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 分析失败");
        }
    }


    /**
     * 将AI响应转换为业务对象
     */
    private ResumeAnalysisResponse convertToResponse(ResumeAnalysisResponseDTO dto, String originalText) {
        ScoreDetail scoreDetail = new ScoreDetail(
                dto.scoreDetail().contentScore(),
                dto.scoreDetail().structureScore(),
                dto.scoreDetail().skillMatchScore(),
                dto.scoreDetail().expressionScore(),
                dto.scoreDetail().projectScore()
        );

        List<Suggestion> suggestions = dto.suggestions().stream()
                .map(s -> new Suggestion(s.category(), s.priority(), s.issue(), s.recommendation()))
                .toList();

        return new ResumeAnalysisResponse(
                dto.overallScore(),
                scoreDetail,
                dto.summary(),
                dto.strengths(),
                suggestions,
                originalText
        );
    }

    /**
     * 将业务响应对象转换为数据库实体
     */
    private ResumeAnalysis convertToEntity(Long resumeId, ResumeAnalysisResponse response) {
        ScoreDetail scoreDetail = response.scoreDetail();
        return new ResumeAnalysis()
                .setResumeId(resumeId)
                .setOverallScore(response.overallScore())
                .setContentScore(scoreDetail.contentScore())
                .setStructureScore(scoreDetail.structureScore())
                .setSkillMatchScore(scoreDetail.skillMatchScore())
                .setExpressionScore(scoreDetail.expressionScore())
                .setProjectScore(scoreDetail.projectScore())
                .setSuggestionsJson(JsonUtils.toJson(response.suggestions()))
                .setStrengthsJson(JsonUtils.toJson(response.strengths()))
                .setSummary(response.summary());
    }
}
