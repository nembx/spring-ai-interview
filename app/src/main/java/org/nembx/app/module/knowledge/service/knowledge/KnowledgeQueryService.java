package org.nembx.app.module.knowledge.service.knowledge;


import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.dto.RetrievalContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lian
 */

@Service
@Slf4j
public class KnowledgeQueryService {
    private static final String NO_RESULT_RESPONSE = "抱歉，在选定的知识库中未检索到相关信息。请换一个更具体的关键词或补充上下文后再试。";

    private final ChatClient chatClient;
    private final KnowledgeVectorService knowledgeVectorService;
    private final PromptTemplate systemPromptTemplate;
    private final PromptTemplate userPromptTemplate;

    public KnowledgeQueryService(
            ChatClient.Builder chatClient,
            KnowledgeVectorService knowledgeVectorService,
            @Value("classpath:prompt/knowledge_system_prompt.st") Resource systemPromptTemplate,
            @Value("classpath:prompt/knowledge_user_prompt.st") Resource userPromptTemplate
    ) throws IOException {
        this.chatClient = chatClient.build();
        this.knowledgeVectorService = knowledgeVectorService;
        this.systemPromptTemplate = new PromptTemplate(systemPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
        this.userPromptTemplate = new PromptTemplate(userPromptTemplate.getContentAsString(StandardCharsets.UTF_8));
    }

    public String answerQuestion(Long knowledgeId, String question) {
        return answerQuestion(List.of(knowledgeId), question);
    }

    public String answerQuestion(List<Long> knowledgeIds, String question) {
        RetrievalContext ctx = retrieve(knowledgeIds, question);
        if (ctx == null) {
            return NO_RESULT_RESPONSE;
        }

        try {
            String answer = chatClient
                    .prompt()
                    .system(ctx.systemPrompt())
                    .user(ctx.userPrompt())
                    .call()
                    .content();
            answer = normalizeAnswer(answer);
            log.info("知识库回答成功, 回复: {}", answer);
            return answer;
        } catch (Exception e) {
            log.error("知识库查询失败", e);
            throw new BusinessException(ErrorCode.KNOWLEDGE_QUERY_ERROR, "知识库查询失败");
        }
    }

    public Flux<String> answerQuestionStream(List<Long> knowledgeIds, String question) {
        RetrievalContext ctx = retrieve(knowledgeIds, question);
        if (ctx == null) {
            return Flux.just(NO_RESULT_RESPONSE);
        }

        return chatClient.prompt()
                .system(ctx.systemPrompt())
                .user(ctx.userPrompt())
                .stream()
                .content()
                .doOnNext(token -> log.debug("[流式输出] {}", token))
                .doOnComplete(() -> log.info("[流式输出完成]"))
                .doOnError(e -> log.error("知识库流式查询失败", e));
    }

    /**
     * 检索知识库并构建 prompt 上下文，若参数无效或未检索到结果返回 null
     */
    private RetrievalContext retrieve(List<Long> knowledgeIds, String question) {
        log.info("开始查询知识库, 问题: {}", question);

        if (CollectionUtil.isEmpty(knowledgeIds) || question == null) {
            log.warn("查询知识库失败, 参数为空");
            return null;
        }

        question = question.trim();

        List<Document> documents = knowledgeVectorService.similaritySearch(question, knowledgeIds, 5, 0.7);
        if (CollectionUtil.isEmpty(documents)) {
            log.warn("查询知识库失败, 未找到相关知识");
            return null;
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--\n\n"));
        log.info("查询知识库成功, 结果数量: {}", documents.size());
        log.debug("检索上下文: {}", context);

        String systemPrompt = systemPromptTemplate.render();
        String userPrompt = userPromptTemplate.render(Map.of("question", question, "context", context));

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
