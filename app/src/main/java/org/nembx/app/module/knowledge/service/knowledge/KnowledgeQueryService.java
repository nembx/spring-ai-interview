package org.nembx.app.module.knowledge.service.knowledge;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.dto.RetrievalContext;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.nembx.app.module.knowledge.properties.VectorProperties;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeQueryService {
    private static final String NO_RESULT_RESPONSE = "抱歉，在选定的知识库中未检索到相关信息。请换一个更具体的关键词或补充上下文后再试。";

    private final AiClient aiClient;

    private final AiPromptManager aiPromptManager;

    private final KnowledgeVectorService knowledgeVectorService;

    private final VectorProperties vectorProperties;

    public String answerQuestion(Long knowledgeId, String question) {
        return answerQuestion(List.of(knowledgeId), question, null);
    }

    public String answerQuestion(List<Long> knowledgeIds, String question, List<RagMessage> ragMessages) {
        RetrievalContext ctx = retrieve(knowledgeIds, question, ragMessages);
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
        RetrievalContext ctx = retrieve(knowledgeIds, question, ragMessages);
        if (ctx == null) {
            return Flux.just(NO_RESULT_RESPONSE);
        }

        return aiClient.stream(ctx.systemPrompt(), ctx.userPrompt())
                .doOnNext(token -> log.debug("[流式输出] {}", token))
                .doOnComplete(() -> log.info("[流式输出完成]"))
                .doOnError(e -> log.error("知识库流式查询失败", e));
    }

    /**
     * 检索知识库并构建 prompt 上下文，若参数无效或未检索到结果返回 null
     */
    private RetrievalContext retrieve(List<Long> knowledgeIds, String question, List<RagMessage> ragMessages) {
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

        question = question.trim();

        List<Document> documents = knowledgeVectorService.similaritySearch(
                question, knowledgeIds, vectorProperties.getTopK(), vectorProperties.getMinScore());
        if (CollectionUtil.isEmpty(documents)) {
            log.warn("查询知识库失败, 未找到相关知识");
            return null;
        }

        // 检索结果去重
        List<Document> uniqueDocuments = documents.stream()
                .filter(doc -> doc.getText() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                doc -> doc.getMetadata().getOrDefault("chunk_hash",
                                        doc.getText() != null ? doc.getText().trim() : null).toString(),
                                doc -> doc,
                                (left, right) -> left
                        ),
                        map -> map.values().stream().toList()
                ));

        // 组成分块内容
        String context = IntStream.range(0, uniqueDocuments.size())
                .mapToObj(i -> {
                    Document doc = uniqueDocuments.get(i);
                    return """
                            [片段 %d]
                            来源文件: %s
                            知识库ID: %s
                            分块序号: %s
                            内容:
                            %s
                            """.formatted(
                            i + 1,
                            doc.getMetadata().getOrDefault("file_name", "未知文件"),
                            doc.getMetadata().getOrDefault("kb_id", "未知"),
                            doc.getMetadata().getOrDefault("chunk_index", "未知"),
                            doc.getText() != null ? doc.getText().trim() : null
                    );
                })
                .collect(Collectors.joining("\n\n---\n\n"));
        log.info("查询知识库成功, 结果数量: {}", documents.size());
        log.debug("检索上下文: {}", context);

        String systemPrompt = aiPromptManager.render("knowledge_system_prompt");
        String userPrompt = aiPromptManager.render("knowledge_user_prompt",
                Map.of("question", question, "context", context, "history", historyContext));

        return new RetrievalContext(systemPrompt, userPrompt);
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
