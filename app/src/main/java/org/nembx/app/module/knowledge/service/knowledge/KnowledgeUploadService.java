package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.FileType;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.service.DocumentParseService;
import org.nembx.app.common.service.FileCheckService;
import org.nembx.app.common.utils.FileHashUtils;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeListenerDTO;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeSaveDTO;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.task.entity.res.TaskSubmitResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeUploadService {
    private final DocumentParseService documentParseService;

    private final KnowledgeManageService knowledgeManageService;

    private final KnowledgeFileService knowledgeFileService;

    private final ApplicationEventPublisher eventPublisher;

    private final FileCheckService fileCheckService;

    @Transactional(rollbackFor = Exception.class)
    public TaskSubmitResponse uploadAndParse(MultipartFile file, String category) {
        String contentType = file.getContentType();
        Long size = file.getSize();
        String originalFilename = file.getOriginalFilename();

        boolean isValid = fileCheckService.isRealValid(file, FileType.KNOWLEDGE);
        if (!isValid) {
            String fileExtension = fileCheckService.getFileExtension(originalFilename);
            log.error("不支持格式: {}, 请上传正确的知识库文件", fileExtension);
            throw new BusinessException(ErrorCode.UPLOAD_FAIL, "请上传正确的知识库文件");
        }

        String content = documentParseService.parseContent(file);
        log.info("解析成功, 文件名为: {}, 内容长度: {} 字符", originalFilename, content.length());

        String hash = FileHashUtils.calculateHash(file);
        log.info("文件hash: {}", hash);

        Knowledge sameHash = knowledgeManageService.findKnowledgeByFileHash(hash);
        if (sameHash != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件已存在");
        }

        // 同名文件：更新已有记录
        Knowledge existing = knowledgeManageService.findLatestByName(originalFilename, category);
        if (existing != null) {
            String fileKey = knowledgeFileService.uploadFile(file);
            String fileUrl = knowledgeFileService.getFileUrl(fileKey);

            knowledgeManageService.replaceKnowledge(existing.getId(),
                    new KnowledgeSaveDTO(hash, originalFilename, category, content, size, contentType, null, null, LocalDateTime.now()));
            knowledgeManageService.updateKnowledgeStorge(existing.getId(), fileKey, fileUrl);

            // 发布事件 -> 走增量向量化
            eventPublisher.publishEvent(new KnowledgeListenerDTO(existing.getId(), content));
            return new TaskSubmitResponse(existing.getId(), TaskResourceType.KNOWLEDGE.getValue(), TaskStatus.PENDING);
        }

        Long knowledgeId = knowledgeManageService.saveKnowledge(
                new KnowledgeSaveDTO(hash, originalFilename, category,
                        content, size, contentType,
                        null, null, LocalDateTime.now())
        );
        log.info("保存成功, 文件名为: {}", originalFilename);

        String fileKey = knowledgeFileService.uploadFile(file);
        String fileUrl = knowledgeFileService.getFileUrl(fileKey);
        knowledgeManageService.updateKnowledgeStorge(knowledgeId, fileKey, fileUrl);
        log.info("上传成功, 文件名为: {}", originalFilename);

        // 发布事件
        eventPublisher.publishEvent(new KnowledgeListenerDTO(knowledgeId, content));
        log.info("发布事件成功, 文件名为: {}", originalFilename);
        return new TaskSubmitResponse(knowledgeId, TaskResourceType.KNOWLEDGE.getValue(), TaskStatus.PENDING);
    }
}
