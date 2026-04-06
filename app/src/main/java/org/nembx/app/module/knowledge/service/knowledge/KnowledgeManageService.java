package org.nembx.app.module.knowledge.service.knowledge;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeListDTO;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeSaveDTO;
import org.nembx.app.module.knowledge.entity.res.KnowledgeResponse;
import org.nembx.app.module.knowledge.repository.KnowledgeRepository;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.nembx.app.module.task.service.TaskProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;



/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeManageService implements TaskProvider {
    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeResponse getKnowledgeById(Long knowledgeId) {
        Knowledge knowledge = getOneById(knowledgeId);
        return new KnowledgeResponse(
                knowledge.getId(),
                knowledge.getFileName(),
                knowledge.getCategory(),
                knowledge.getFileSize(),
                knowledge.getFileType(),
                knowledge.getUploadTime()
        );
    }

    @Override
    public TaskResourceType supportType() {
        return TaskResourceType.KNOWLEDGE;
    }

    @Override
    public TaskStatusResponse getTaskStatus(Long knowledgeId) {
        Knowledge knowledge = getOneById(knowledgeId);
        return new TaskStatusResponse(
                knowledge.getId(),
                TaskResourceType.KNOWLEDGE.getValue(),
                knowledge.getFileName(),
                knowledge.getTaskStatus(),
                knowledge.getUploadTime()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeStatus(Long knowledgeId, TaskStatus status) {
        Knowledge knowledge = knowledgeRepository.findById(knowledgeId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在"));
        knowledge.setTaskStatus(status);
        log.debug("更新知识状态成功, 知识ID: {}, 状态: {}", knowledgeId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeStorge(Long knowledgeId, String key, String url) {
        Knowledge knowledge = knowledgeRepository.findById(knowledgeId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在"));
        knowledge.setStorageKey(key);
        knowledge.setStorageUrl(url);
        log.debug("更新知识存储成功, 知识ID: {}, 存储Key: {}, 存储URL: {}", knowledgeId, key, url);
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceKnowledge(Long knowledgeId, KnowledgeSaveDTO dto) {
        Knowledge knowledge = getOneById(knowledgeId);
        knowledge.setFileHash(dto.fileHash())
                .setFileName(dto.fileName())
                .setCategory(dto.category())
                .setContent(dto.content())
                .setFileSize(dto.fileSize())
                .setFileType(dto.fileType())
                .setUploadTime(LocalDateTime.now())
                .setTaskStatus(TaskStatus.PENDING);
    }

    public Knowledge findLatestByName(String fileName, String category) {
        return knowledgeRepository.findFirstByFileNameAndCategoryOrderByUploadTimeDesc(fileName, category)
                .orElse(null);
    }

    public Knowledge getOneById(Long knowledgeId) {
        if (knowledgeId == null || knowledgeId <= 0) {
            log.warn("获取知识失败, 知识ID非法");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "获取知识失败");
        }
        return knowledgeRepository.findById(knowledgeId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND, "获取知识失败"));
    }

    public List<KnowledgeListDTO> toListDTO(List<Knowledge> knowledgeList) {
        if (CollectionUtil.isEmpty(knowledgeList)) {
            log.warn("获取知识列表失败, 知识列表为空");
            return List.of();
        }
        return knowledgeList.stream().map(knowledge -> new KnowledgeListDTO(
                knowledge.getId(), knowledge.getFileName(),
                knowledge.getCategory(), knowledge.getFileSize(),
                knowledge.getFileType(), knowledge.getUploadTime()
        )).toList();
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
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

    public List<KnowledgeListDTO> findAllByOrderByUploadTimeDesc() {
        List<Knowledge> knowledgeList = knowledgeRepository.findAllByOrderByUploadTimeDesc();
        return knowledgeList.stream().map(knowledge -> new KnowledgeListDTO(
                knowledge.getId(), knowledge.getFileName(),
                knowledge.getCategory(), knowledge.getFileSize(),
                knowledge.getFileType(), knowledge.getUploadTime()
        )).toList();
    }

    public List<KnowledgeListDTO> findAllByCategory(String category) {
        List<Knowledge> knowledgeList = knowledgeRepository.findAllByCategory(category);
        return knowledgeList.stream().map(knowledge -> new KnowledgeListDTO(
                knowledge.getId(), knowledge.getFileName(),
                knowledge.getCategory(), knowledge.getFileSize(),
                knowledge.getFileType(), knowledge.getUploadTime()
        )).toList();
    }
}
