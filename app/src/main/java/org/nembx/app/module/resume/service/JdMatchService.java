package org.nembx.app.module.resume.service;


import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.utils.JsonUtils;
import org.nembx.app.module.resume.entity.dto.JdMatchResponseDTO;
import org.nembx.app.module.resume.entity.pojo.JdMatch;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.entity.res.JdMatchResponse;
import org.nembx.app.module.resume.repository.JdMatchRepository;
import org.nembx.app.module.resume.service.resume.ResumeManageService;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JdMatchService {
    private final JdMatchRepository jdMatchRepository;

    private final ResumeManageService resumeManageService;

    private final AiClient aiClient;

    private final AiPromptManager aiPromptManager;

    private final BeanOutputConverter<JdMatchResponseDTO> outputConverter = new BeanOutputConverter<>(
            JdMatchResponseDTO.class);

    @Transactional(rollbackFor = Exception.class)
    public void match(Long resumeId, String jdContent) {
        // 获取简历内容
        Resume resume = resumeManageService.getOneById(resumeId);
        String resumeText = resume.getContent();

        String systemPrompt = aiPromptManager.render("resume/jd_system_prompt");
        String userPrompt = aiPromptManager.render("resume/jd_user_prompt", Map.of(
                "resumeText", resumeText,
                "jdContent", jdContent
        ));

        JdMatchResponseDTO jdMatchResponse = aiClient.call(systemPrompt, userPrompt, outputConverter);
        // 保存匹配结果
        saveJdMatch(resumeId, jdContent, jdMatchResponse);

        log.debug("JD匹配成功, 简历ID: {}, 职位描述: {}", resumeId, jdContent);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveJdMatch(Long resumeId, String jdContent, JdMatchResponseDTO jdMatchResponse) {
        JdMatch jdMatch = new JdMatch();
        jdMatch.setResumeId(resumeId)
                .setJdContent(jdContent)
                .setOverallScore(jdMatchResponse.overallScore())
                .setMatchScore(jdMatchResponse.matchScore())
                .setSuggestionsJson(JsonUtils.toJson(jdMatchResponse.suggestions()))
                .setMissingSkillsJson(JsonUtils.toJson(jdMatchResponse.missingSkills()));
        jdMatchRepository.save(jdMatch);
    }

    public JdMatchResponse getJdMatchResult(Long resumeId) {
        JdMatch jdMatch = jdMatchRepository.findByResumeId(resumeId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND, "未找到职位匹配结果")
        );
        return new JdMatchResponse(
                jdMatch.getJdContent(),
                jdMatch.getOverallScore(),
                jdMatch.getMatchScore(),
                JsonUtils.fromJson(jdMatch.getSuggestionsJson(), new TypeReference<>() {
                }),
                JsonUtils.fromJson(jdMatch.getMissingSkillsJson(), new TypeReference<>() {
                })
        );
    }
}
