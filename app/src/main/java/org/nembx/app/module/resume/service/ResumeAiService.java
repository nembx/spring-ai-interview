package org.nembx.app.module.resume.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.utils.JsonUtils;
import org.nembx.app.module.resume.entity.pojo.ResumeAnalysis;
import org.nembx.app.module.resume.entity.dto.ResumeAnalysisResponseDTO;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse.ScoreDetail;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse.Suggestion;
import org.nembx.app.module.resume.repository.ResumeAnalysisRepository;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.nembx.app.common.exception.ErrorCode.AI_CALL_ERROR;
import static org.nembx.app.common.exception.ErrorCode.BAD_REQUEST;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeAiService {
    private final AiClient aiClient;
    private final AiPromptManager aiPromptManager;
    private final ResumeManageService resumeManageService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    private final BeanOutputConverter<ResumeAnalysisResponseDTO> outputConverter = new BeanOutputConverter<>(ResumeAnalysisResponseDTO.class);

    public void analyzeResume(Long resumeId, String resumeText) {
        log.info("开始调用大模型分析简历, 简历ID: {}", resumeId);
        if (resumeText == null) {
            log.error("简历文本为空");
            throw new BusinessException(BAD_REQUEST, "简历文本为空");
        }
        resumeManageService.updateResumeStatus(resumeId, TaskStatus.PROCESSING);

        try {
            String systemPrompt = aiPromptManager.render("resume_system_prompt");
            String userPrompt = aiPromptManager.render("resume_user_prompt", Map.of("resumeText", resumeText));

            ResumeAnalysisResponseDTO dto = aiClient.call(systemPrompt, userPrompt, outputConverter);
            if (dto == null) {
                log.error("AI 响应解析失败");
                resumeManageService.updateResumeStatus(resumeId, TaskStatus.FAILED);
                return;
            }
            log.debug("AI响应解析成功: overallScore={}", dto.overallScore());

            ResumeAnalysisResponse result = convertToResponse(dto, resumeText);
            log.info("简历分析完成，总分: {}", result.overallScore());

            ResumeAnalysis resumeAnalysis = convertToEntity(resumeId, result);
            resumeAnalysisRepository.save(resumeAnalysis);
            log.info("保存简历分析结果到数据库");

            resumeManageService.updateResumeStatus(resumeId, TaskStatus.COMPLETED);
        } catch (Exception e) {
            log.error("调用 AI 接口分析简历失败, 简历ID: {}", resumeId, e);
            resumeManageService.updateResumeStatus(resumeId, TaskStatus.FAILED);
            throw new BusinessException(AI_CALL_ERROR, "AI 分析失败");
        }
    }

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
