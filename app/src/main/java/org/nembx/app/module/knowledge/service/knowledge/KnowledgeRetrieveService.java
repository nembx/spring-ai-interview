package org.nembx.app.module.knowledge.service.knowledge;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiPromptManager;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.module.knowledge.entity.dto.RetrievalContext;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.nembx.app.module.knowledge.properties.VectorProperties;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

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
public class KnowledgeRetrieveService {
    private final KnowledgeVectorService knowledgeVectorService;

    private final VectorProperties vectorProperties;

    private final AiPromptManager aiPromptManager;

    private final KnowledgeRerankService knowledgeRerankService;

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

        question = question.trim();
        List<Document> documents = knowledgeVectorService.similaritySearch(
                question, knowledgeIds, vectorProperties.getTopK(), vectorProperties.getMinScore());
        if (CollectionUtil.isEmpty(documents)) {
            log.warn("查询知识库失败, 未找到相关知识");
            return null;
        }

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
        // 召回后rerank重排序
        List<Document> rerankedDocuments = knowledgeRerankService.rerank(question, uniqueDocuments);
        List<Document> finalDocuments = CollectionUtil.isEmpty(rerankedDocuments) ? uniqueDocuments : rerankedDocuments;

        String context = IntStream.range(0, finalDocuments.size())
                .mapToObj(i -> {
                    Document doc = finalDocuments.get(i);
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
        log.info("查询知识库成功, 召回数量: {}, 最终数量: {}", documents.size(), finalDocuments.size());
        log.debug("检索上下文: {}", context);

        String systemPrompt = aiPromptManager.render("knowledge_system_prompt");
        String userPrompt = aiPromptManager.render("knowledge_user_prompt",
                Map.of("question", question, "context", context, "history", historyContext));

        return new RetrievalContext(systemPrompt, userPrompt);
    }
}
