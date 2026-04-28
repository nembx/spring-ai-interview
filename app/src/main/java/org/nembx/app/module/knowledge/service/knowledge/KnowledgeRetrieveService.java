package org.nembx.app.module.knowledge.service.knowledge;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.entity.dto.RetrievalContext;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeRetrieveService {
    private final KnowledgeSearchService knowledgeSearchService;

    private final AiPromptManager aiPromptManager;

    public RetrievalContext retrieve(List<Long> knowledgeIds, String question, List<RagMessage> ragMessages) {
        log.info("开始查询知识库, 问题: {}", question);

        if (CollectionUtil.isEmpty(knowledgeIds) || question == null) {
            log.warn("查询知识库失败, 参数为空");
            return null;
        }

        String historyContext = "";
        if (CollectionUtil.isNotEmpty(ragMessages)) {
            historyContext = ragMessages.stream()
                    .map(msg -> (msg.getType() == MessageType.USER ? "用户: " : "助手: ") + msg.getContent())
                    .collect(Collectors.joining("\n"));
            log.debug("组装的历史记录:\n{}", historyContext);
        }

        String context = knowledgeSearchService.search(knowledgeIds, question.trim());
        if (context.isEmpty()) {
            log.warn("查询知识库失败, 未找到相关知识");
            return null;
        }

        String systemPrompt = aiPromptManager.render("knowledge/system_prompt");
        String userPrompt = aiPromptManager.render("knowledge/user_prompt",
                Map.of("question", question, "context", context, "history", historyContext));

        return new RetrievalContext(systemPrompt, userPrompt);
    }
}
