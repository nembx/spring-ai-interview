package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeDeleteService {
    private final KnowledgeVectorService knowledgeVectorService;

    private final KnowledgeManageService knowledgeManageService;

    private final KnowledgeFileService knowledgeFileService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledge(Long knowledgeId) {
        Knowledge knowledge = knowledgeManageService.getOneById(knowledgeId);
        if (knowledge == null) {
            log.warn("删除知识失败, 知识不存在");
            return;
        }
        knowledgeManageService.deleteKnowledge(knowledgeId);
        log.info("数据库删除知识库成功, knowledgeId: {}", knowledgeId);

        knowledgeFileService.deleteFile(knowledge.getStorageKey());
        log.info("文件系统删除知识库成功, knowledgeId: {}", knowledgeId);

        try {
            knowledgeVectorService.deleteKnowledge(knowledgeId);
            log.info("向量数据库删除知识库成功, knowledgeId: {}", knowledgeId);
        } catch (Exception e) {
            log.error("向量数据库删除知识库失败, knowledgeId: {}", knowledgeId);
            throw new RuntimeException(e);
        }
    }
}
