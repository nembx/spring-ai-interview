package org.nembx.app.module.knowledge.repository;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lian
 */

@Repository
@Slf4j
@RequiredArgsConstructor
public class VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void deleteByKnowledgeId(Long knowledgeId) {
        log.info("开始删除向量, 知识ID: {}", knowledgeId);
        try {
            String sql = """
                        DELETE FROM vector_store
                         WHERE (metadata->>'kb_id')::bigint = ?
                    """;

            // 执行SQL
            int deletedRows = jdbcTemplate.update(sql, knowledgeId);

            if (deletedRows > 0)
                log.info("删除向量成功, 删除行数: {}", deletedRows);
            else
                log.info("未找到相关向量数据，无需删除, 知识ID: {}", knowledgeId);
        } catch (Exception e) {
            log.error("删除向量数据失败, 知识ID: {}", knowledgeId, e);
            throw new RuntimeException(e);
        }
    }

    public List<String> findChunksHashByKnowledgeId(Long knowledgeId) {
        log.info("开始查询向量, 知识ID: {}", knowledgeId);
        try {
            String sql = """
                        SELECT metadata->>'chunk_hash'
                          FROM vector_store
                         WHERE (metadata->>'kb_id')::bigint = ?
                    """;

            // 执行SQL
            return jdbcTemplate.query(
                    sql, (rs, rowNum) -> rs.getString(1),
                    knowledgeId);
        } catch (Exception e) {
            log.error("查询向量分块哈希失败, 知识ID: {}", knowledgeId, e);
            throw new RuntimeException(e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChunksByKnowledgeIdAndChunkHashList(Long knowledgeId, List<String> chunkHash) {
        if (CollectionUtil.isEmpty(chunkHash))
            return;
        try {
            int batchSize = 500;
            for (int i = 0; i < chunkHash.size(); i += batchSize) {
                List<String> batch = chunkHash.subList(i, Math.min(i + batchSize, chunkHash.size()));
                String placeholders = String.join(",", batch.stream().map(h -> "?").toList());
                String sql = """
                            DELETE FROM vector_store
                                   WHERE (metadata->>'kb_id')::bigint = ?
                                     AND metadata->>'chunk_hash' IN (%s)
                        """.formatted(placeholders);
                List<Object> args = new ArrayList<>();
                args.add(knowledgeId);
                args.addAll(batch);
                jdbcTemplate.update(sql, args.toArray());
            }
        } catch (Exception e) {
            log.error("删除向量数据失败, 知识ID: {}, chunkHash: {}", knowledgeId, chunkHash, e);
            throw new RuntimeException(e);
        }
    }
}
