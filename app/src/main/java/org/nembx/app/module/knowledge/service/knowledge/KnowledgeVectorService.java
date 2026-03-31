package org.nembx.app.module.knowledge.service.knowledge;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.utils.FileHashUtils;
import org.nembx.app.module.knowledge.repository.VectorRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeVectorService {
    private final VectorStore vectorStore;

    private final TextSplitter textSplitter = new TokenTextSplitter();

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

            Map<String, Document> chunkMap = new LinkedHashMap<>();
            AtomicInteger index = new AtomicInteger();
            // 添加知识ID
            chunks.forEach(chunk -> {
                        String text = chunk.getText();
                        if (text == null) return;
                        String hash = FileHashUtils.hashChunk(text);
                        Map<String, Object> metadata = chunk.getMetadata();
                        metadata.put("kb_id", knowledgeId);
                        metadata.put("chunk_hash", hash);
                        metadata.put("chunk_index", index.getAndIncrement());
                        chunkMap.put(hash, chunk);
                    }
            );

            // 查询旧 chunk
            List<String> storedChunkHashes = vectorRepository.findChunksHashByKnowledgeId(knowledgeId);
            boolean hasLegacyChunks = storedChunkHashes.stream()
                    .anyMatch(hash -> hash == null || hash.isBlank());
            if (hasLegacyChunks) {
                log.debug("检测到缺少 chunk_hash 的旧向量数据, 将全量重建知识向量, 知识ID: {}", knowledgeId);
                vectorRepository.deleteByKnowledgeId(knowledgeId);
                storedChunkHashes = List.of();
            }
            Set<String> oldHashes = storedChunkHashes.stream()
                    .filter(hash -> hash != null && !hash.isBlank())
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);
            Set<String> newHashes = chunkMap.keySet();

            // 计算差异
            List<String> toDelete = oldHashes.stream().filter(h -> !newHashes.contains(h)).toList();
            List<Document> toAdd = newHashes.stream()
                    .filter(h -> !oldHashes.contains(h))
                    .map(chunkMap::get)
                    .toList();

            // 删除旧的
            vectorRepository.deleteChunksByKnowledgeIdAndChunkHashList(knowledgeId, toDelete);

            // 只新增差量
            for (int i = 0; i < toAdd.size(); i += MAX_BATCH_SIZE) {
                List<Document> batch = toAdd.subList(i, Math.min(i + MAX_BATCH_SIZE, toAdd.size()));
                vectorStore.add(batch);
            }
            knowledgeManageService.updateKnowledgeStatus(knowledgeId, TaskStatus.COMPLETED);
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
                log.debug("过滤相似度分数: {}", minScore);
                builder.similarityThreshold(minScore);
            }
            if (CollectionUtil.isNotEmpty(knowledgeIds)) {
                log.debug("过滤知识ID: {}", knowledgeIds);
                builder.filterExpression("kb_id IN [%s]".formatted(
                        String.join(",", knowledgeIds.stream().map(String::valueOf).toList())));
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
