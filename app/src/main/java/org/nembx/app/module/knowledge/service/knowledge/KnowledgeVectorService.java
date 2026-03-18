package org.nembx.app.module.knowledge.service.knowledge;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.module.knowledge.repository.VectorRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeVectorService {
    private final VectorStore vectorStore;

    private TextSplitter textSplitter = new TokenTextSplitter();

    private final VectorRepository vectorRepository;

    private final KnowledgeManageService knowledgeManageService;

    private static final int MAX_BATCH_SIZE = 10;

    @Transactional(rollbackFor = Exception.class)
    public void vectorizeKnowledge(Long knowledgeId, String content) {
        knowledgeManageService.updateKnowledgeStatus(knowledgeId, TaskStatus.PROCESSING);
        log.info("开始向量化知识, 知识ID: {}", knowledgeId);
        try {
            // 分块
            List<Document> chunks = textSplitter.apply(List.of(new Document(content)));
            log.debug("分块数量: {}", chunks.size());

            // 添加知识ID
            chunks.forEach(chunk ->
                    chunk.getMetadata().put("kb_id", knowledgeId.toString())
            );

            int totalChunks = chunks.size();
            int batchCount = (totalChunks + MAX_BATCH_SIZE - 1) / MAX_BATCH_SIZE; // 向上取整
            log.info("分块数量: {}, 批次数量: {}", totalChunks, batchCount);

            // 分批次向量化
            for (int i = 0; i < batchCount; i++) {
                int start = i * MAX_BATCH_SIZE;
                int end = Math.min(start + MAX_BATCH_SIZE, totalChunks);
                List<Document> batch = chunks.subList(start, end);
                log.info("向量化批次: 第{}批", i);
                vectorStore.add(batch);
            }
            knowledgeManageService.updateKnowledgeStatus(knowledgeId, TaskStatus.COMPLETED);
            log.info("向量化完成, 知识ID: {}", knowledgeId);
        } catch (Exception e) {
            log.error("向量化知识库失败, 知识ID: {}", knowledgeId, e);
            knowledgeManageService.updateKnowledgeStatus(knowledgeId, TaskStatus.FAILED);
            throw new RuntimeException("向量化知识库失败: " + e.getMessage());
        }
    }

    public List<Document> similaritySearch(String query, List<Long> knowledgeIds, int topK, double minScore) {
        log.info("开始相似度搜索, 查询: {}, 知识ID: {}, topK: {}, minScore: {}", query, knowledgeIds, topK, minScore);
        try {
            SearchRequest.Builder builder = SearchRequest
                    .builder()
                    .query(query)
                    .topK(Math.max(topK, 1));
            if (minScore > 0) {
                log.info("过滤相似度分数: {}", minScore);
                builder.similarityThreshold(minScore);
            }
            if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
                log.info("过滤知识ID: {}", knowledgeIds);
                builder.filterExpression("kb_id IN [%s]".formatted(String.join(",", knowledgeIds.stream().map(Object::toString).toList())));
            }
            List<Document> resDocuments = vectorStore.similaritySearch(builder.build());
            if (CollectionUtil.isEmpty(resDocuments)) {
                log.warn("相似度搜索结果为空, 查询: {}, 知识ID: {}", query, knowledgeIds);
                return List.of();
            }
            log.info("相似度搜索完成, 结果数量: {}, 知识ID: {}", resDocuments.size(), knowledgeIds);
            return resDocuments;
        } catch (Exception e) {
            log.warn("相似度搜索失败, 错误信息: {}", e.getMessage());
            return List.of();
        }
    }

    // 删除指定数据库所有的向量
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledge(Long knowledgeId) {
        try {
            vectorRepository.deleteByKnowledgeId(knowledgeId);
            log.info("删除向量成功, 知识ID: {}", knowledgeId);
        } catch (Exception e) {
            throw new RuntimeException("删除向量数据失败: " + e.getMessage(), e);
        }
    }
}
