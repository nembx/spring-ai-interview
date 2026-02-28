package org.nembx.app.module.knowledge.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeManageService {
    private final KnowledgeRepository knowledgeRepository;

    public Knowledge getKnowledgeById(Long knowledgeId) {
        if (knowledgeId == null || knowledgeId <= 0){
            log.warn("获取知识失败, 知识ID非法");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "获取知识失败");
        }
        return knowledgeRepository.findById(knowledgeId).orElse(null);
    }

    @Transactional(rollbackFor = BusinessException.class)
    public void saveKnowledge(Knowledge knowledge) {
        if (knowledge == null){
            log.warn("保存知识失败, 知识为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "保存知识失败");
        }
        knowledgeRepository.save(knowledge);
        log.info("保存知识成功, 知识ID: {}", knowledge.getId());
    }

    @Transactional(rollbackFor = BusinessException.class)
    public void deleteKnowledge(Long knowledgeId) {
        if (knowledgeId == null || knowledgeId <= 0){
            log.warn("删除知识失败, 知识ID非法");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "删除知识失败");
        }
        knowledgeRepository.deleteById(knowledgeId);
        log.info("删除知识成功, 知识ID: {}", knowledgeId);
    }
}
