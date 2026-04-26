package org.nembx.app.module.interview.service;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.ai.SkillRegistry;
import org.nembx.app.common.entity.dto.RetrievalContext;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.module.interview.entity.pojo.InterviewMessage;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeSearchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Lian
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewRetrieveService {
    private final AiPromptManager aiPromptManager;

    private final SkillRegistry skillRegistry;

    private final KnowledgeSearchService knowledgeSearchService;

    public RetrievalContext retrieve(String jdContent, String selectedSkill, String question, String resumeText,
                                     Integer questionCount, Integer maxQuestions,
                                     List<InterviewMessage> messages, List<Long> knowledgeIds) {
        String historyContext = "";
        if (CollectionUtil.isNotEmpty(messages)) {
            historyContext = messages.stream()
                    .map(msg -> (msg.getType() == MessageType.USER ? "用户: " : "助手: ") + msg.getContent())
                    .collect(Collectors.joining("\n"));
            log.debug("组装的历史记录:\n{}", historyContext);
        }

        String knowledgeContext = knowledgeSearchService.search(knowledgeIds, question);

        String skill = Optional.ofNullable(selectedSkill)
                .filter(s -> !s.isBlank())
                .orElse("");
        String skillBody = skillRegistry.getBody(skill);
        String systemPrompt = aiPromptManager.render("interview/system_prompt",
                Map.of("skillBody", skillBody));
        String userPrompt = aiPromptManager.render("interview/user_prompt",
                Map.of("question", question, "jdContent", jdContent, "resumeText", resumeText,
                        "questionCount", questionCount, "maxQuestions", maxQuestions,
                        "history", historyContext, "knowledgeContext", knowledgeContext));

        return new RetrievalContext(systemPrompt, userPrompt);
    }
}
