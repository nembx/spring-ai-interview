package org.nembx.app.module.knowledge.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
            String sql = "DELETE FROM vector_store WHERE metadata->>'kb_id' = ?\n" +
                    "OR (metadata->>'kb_id_long' IS NOT NULL AND (metadata->>'kb_id_long')::bigint = ?)";

            // 执行SQL
            int deletedRows = jdbcTemplate.update(sql, knowledgeId.toString(), knowledgeId);

            if (deletedRows > 0)
                log.info("删除向量成功, 删除行数: {}", deletedRows);
            else
                log.info("未找到相关向量数据，无需删除, 知识ID: {}", knowledgeId);
        } catch (Exception e) {
            log.error("删除向量数据失败, 知识ID: {}", knowledgeId, e);
            throw new RuntimeException("删除向量数据失败", e);
        }
    }
}
