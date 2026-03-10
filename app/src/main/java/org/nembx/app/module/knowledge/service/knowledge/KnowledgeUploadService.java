package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.service.DocumentParseService;
import org.nembx.app.common.utils.FileHashUtils;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.enity.dto.KnowledgeListenerDTO;
import org.nembx.app.module.knowledge.enity.dto.KnowledgeSaveDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
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

    public void uploadAndParse(MultipartFile file, String category) {
        String contentType = file.getContentType();
        Long size = file.getSize();
        String originalFilename = file.getOriginalFilename();

        String hash = FileHashUtils.calculateHash(file);
        log.info("文件hash: {}", hash);

        Knowledge knowledgeByFileHash = knowledgeManageService.findKnowledgeByFileHash(hash);
        if (knowledgeByFileHash != null) {
            log.info("文件已存在, 文件名为: {}", originalFilename);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件已存在");
        }

        String fileKey = knowledgeFileService.uploadFile(file);
        String fileUrl = knowledgeFileService.getFileUrl(fileKey);
        log.info("上传成功, 文件名为: {}", originalFilename);

        Long knowledgeId = knowledgeManageService.saveKnowledge(
                new KnowledgeSaveDTO(hash, originalFilename, category, size, contentType, fileKey, fileUrl, LocalDateTime.now())
        );
        log.info("保存成功, 文件名为: {}", originalFilename);

        String content = documentParseService.parseContent(file);
        log.info("解析成功, 文件名为: {}, 内容为: {}", originalFilename, content);
        // 发布事件
        eventPublisher.publishEvent(new KnowledgeListenerDTO(knowledgeId, content));
        log.info("发布事件成功, 文件名为: {}", originalFilename);
    }
}
