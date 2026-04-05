package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.knowledge.repository.RagSessionRepository;
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

    private final RagSessionRepository ragSessionRepository;

    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledge(Long knowledgeId) {
        try {
            Knowledge knowledge = knowledgeManageService.getOneById(knowledgeId);
            ragSessionRepository.deleteKnowledgeRelations(knowledgeId);
            log.info("删除知识库会话关联成功, knowledgeId: {}", knowledgeId);

            knowledgeVectorService.deleteKnowledge(knowledgeId);
            log.info("向量数据库删除知识库成功, knowledgeId: {}", knowledgeId);

            knowledgeManageService.deleteKnowledge(knowledgeId);
            log.info("数据库删除知识库成功, knowledgeId: {}", knowledgeId);

            knowledgeFileService.deleteFile(knowledge.getStorageKey());
            log.info("文件系统删除知识库成功, knowledgeId: {}", knowledgeId);
        } catch (BusinessException e) {
            log.error("删除知识库失败, knowledgeId: {}", knowledgeId, e);
            throw e;
        } catch (Exception e) {
            log.error("删除知识库失败, knowledgeId: {}", knowledgeId, e);
            throw new BusinessException(ErrorCode.DELETE_FAIL, "删除知识库失败");
        }
    }
}
