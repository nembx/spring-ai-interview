package org.nembx.app.module.knowledge.service.knowledge;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.knowledge.properties.VectorProperties;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 知识库检索服务：向量召回 → 去重 → Rerank → 格式化上下文
 *
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeSearchService {
    private final KnowledgeVectorService knowledgeVectorService;

    private final KnowledgeRerankService knowledgeRerankService;

    private final VectorProperties vectorProperties;

    /**
     * 根据问题检索知识库，返回格式化后的上下文文本。
     * 无结果时返回空字符串。
     */
    public String search(List<Long> knowledgeIds, String question) {
        if (CollectionUtil.isEmpty(knowledgeIds) || question == null || question.isBlank()) {
            return "";
        }

        List<Document> documents = knowledgeVectorService.similaritySearch(
                question.trim(), knowledgeIds, vectorProperties.getTopK(), vectorProperties.getMinScore());
        if (CollectionUtil.isEmpty(documents)) {
            log.info("知识库检索无结果, 问题: {}", question);
            return "";
        }

        List<Document> uniqueDocuments = deduplicate(documents);
        List<Document> rerankedDocuments = knowledgeRerankService.rerank(question, uniqueDocuments);
        List<Document> finalDocuments = CollectionUtil.isEmpty(rerankedDocuments) ? uniqueDocuments : rerankedDocuments;

        log.info("知识库检索成功, 召回数量: {}, 最终数量: {}", documents.size(), finalDocuments.size());
        return formatContext(finalDocuments);
    }

    private List<Document> deduplicate(List<Document> documents) {
        return documents.stream()
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
    }

    private String formatContext(List<Document> documents) {
        return IntStream.range(0, documents.size())
                .mapToObj(i -> {
                    Document doc = documents.get(i);
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
                            doc.getText() != null ? doc.getText().trim() : ""
                    );
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
