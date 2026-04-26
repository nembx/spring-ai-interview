package org.nembx.app.module.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.ai.SkillRegistry;
import org.nembx.app.module.interview.entity.dto.SkillSelectionResult;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewSkillRouter {
    private final AiClient aiClient;

    private final AiPromptManager aiPromptManager;

    private final SkillRegistry skillRegistry;

    private final BeanOutputConverter<SkillSelectionResult> outputConverter = new BeanOutputConverter<>(
            SkillSelectionResult.class);

    public SkillSelectionResult route(String jdContent, String resumeText) {
        String systemPrompt = aiPromptManager.render("interview/skill_router_system",
                Map.of("skillRegistry", skillRegistry.renderRegistry()));
        String userPrompt = aiPromptManager.render("interview/skill_router_user", Map.of(
                "resumeText", resumeText,
                "jdContent", jdContent
        ));
        SkillSelectionResult result = aiClient.call(systemPrompt, userPrompt, outputConverter);
        log.info("[Skill路由完成] skill={}, reason={}", result.skill(), result.reason());
        return result;
    }
}
