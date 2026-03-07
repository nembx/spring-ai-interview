package org.nembx.app.module.knowledge.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.enity.dto.KnowledgeListDTO;
import org.nembx.app.module.knowledge.enity.dto.KnowledgeSaveDTO;
import org.nembx.app.module.knowledge.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeManageService {
    private final KnowledgeRepository knowledgeRepository;

    public Knowledge getKnowledgeById(Long knowledgeId) {
        if (knowledgeId == null || knowledgeId <= 0) {
            log.warn("获取知识失败, 知识ID非法");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "获取知识失败");
        }
        return knowledgeRepository.findById(knowledgeId).orElse(null);
    }

    public List<Knowledge> findAllKnowledge(String KnowledgeIds) {
        List<Long> idList = Arrays.stream(KnowledgeIds.split(","))
                .map(String::trim)     // 去掉可能存在的空格，例如 "1, 2"
                .map(Long::valueOf)    // 转换为 Long 类型（如果你的 ID 是 String，去掉这行即可）
                .toList();
        return knowledgeRepository.findAllById(idList);
    }

    public List<KnowledgeListDTO> toListDTO(List<Knowledge> knowledgeList) {
        if (CollectionUtil.isEmpty(knowledgeList)) {
            log.warn("获取知识列表失败, 知识列表为空");
            return null;
        }
        return knowledgeList.stream().map(knowledge -> new KnowledgeListDTO(
                knowledge.getId(), knowledge.getFileName(),
                knowledge.getCategory(), knowledge.getFileSize(),
                knowledge.getFileType(), knowledge.getUploadTime()
        )).toList();
    }

    @Transactional(rollbackFor = BusinessException.class)
    public Long saveKnowledge(KnowledgeSaveDTO knowledgeSaveDTO) {
        if (knowledgeSaveDTO == null) {
            log.warn("保存知识失败, 知识为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "保存知识失败");
        }
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(knowledgeSaveDTO, knowledge);
        knowledgeRepository.save(knowledge);
        log.info("保存知识成功, 知识ID: {}", knowledge.getId());
        return knowledge.getId();
    }

    @Transactional(rollbackFor = BusinessException.class)
    public void deleteKnowledge(Long knowledgeId) {
        if (knowledgeId == null || knowledgeId <= 0) {
            log.warn("删除知识失败, 知识ID非法");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "删除知识失败");
        }
        knowledgeRepository.deleteById(knowledgeId);
        log.info("删除知识成功, 知识ID: {}", knowledgeId);
    }

    public Knowledge findKnowledgeByFileHash(String fileHash) {
        if (fileHash == null || fileHash.isEmpty()) {
            log.warn("获取知识失败, 文件Hash为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "获取知识失败");
        }
        return knowledgeRepository.findKnowledgeByFileHash(fileHash).orElse(null);
    }
}
