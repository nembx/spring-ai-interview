package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.entity.dto.RetrievalContext;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeQueryService {
    private static final String NO_RESULT_RESPONSE = "抱歉，在选定的知识库中未检索到相关信息。请换一个更具体的关键词或补充上下文后再试。";

    private final AiClient aiClient;

    private final KnowledgeRetrieveService knowledgeRetrieveService;

    public String answerQuestion(Long knowledgeId, String question) {
        return answerQuestion(List.of(knowledgeId), question, null);
    }

    public String answerQuestion(List<Long> knowledgeIds, String question, List<RagMessage> ragMessages) {
        RetrievalContext ctx = knowledgeRetrieveService.retrieve(knowledgeIds, question, ragMessages);
        if (ctx == null) {
            return NO_RESULT_RESPONSE;
        }
        try {
            String answer = aiClient.call(ctx.systemPrompt(), ctx.userPrompt());
            answer = normalizeAnswer(answer);
            log.info("知识库回答成功, 回复: {}", answer);
            return answer;
        } catch (Exception e) {
            log.error("知识库查询失败", e);
            throw new BusinessException(ErrorCode.KNOWLEDGE_QUERY_ERROR, "知识库查询失败");
        }
    }

    public Flux<String> answerQuestionStream(List<Long> knowledgeIds, String question, List<RagMessage> ragMessages) {
        RetrievalContext ctx = knowledgeRetrieveService.retrieve(knowledgeIds, question, ragMessages);
        if (ctx == null) {
            return Flux.just(NO_RESULT_RESPONSE);
        }
        return aiClient.stream(ctx.systemPrompt(), ctx.userPrompt())
                .doOnNext(token -> log.debug("[流式输出] {}", token))
                .doOnComplete(() -> log.info("[流式输出完成]"))
                .doOnError(e -> log.error("知识库流式查询失败", e));
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return NO_RESULT_RESPONSE;
        }
        if (answer.contains("没有找到相关信息")
                || answer.contains("超出知识库范围")
                || answer.contains("无法根据提供内容回答")) {
            return NO_RESULT_RESPONSE;
        }
        return answer.trim();
    }
}
