package org.nembx.app.module.resume.service.resume;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.service.DocumentParseService;
import org.nembx.app.common.service.FileCheckService;
import org.nembx.app.common.utils.FileHashUtils;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.entity.dto.ResumeListenerDTO;
import org.nembx.app.module.resume.entity.dto.ResumeSaveDTO;
import org.nembx.app.module.task.entity.res.TaskSubmitResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.nembx.app.common.exception.ErrorCode.BAD_REQUEST;
import static org.nembx.app.common.exception.ErrorCode.UPLOAD_FAIL;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeUploadService {
    private final DocumentParseService documentParseService;

    private final ResumeFileService resumeFileService;

    private final ResumeManageService resumeManageService;

    private final FileCheckService fileCheckService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public TaskSubmitResponse uploadAndAnalyze(MultipartFile file) {
        String contentType = file.getContentType();
        Long size = file.getSize();
        String originalFilename = file.getOriginalFilename();
        // 检查文件
        boolean valid = fileCheckService.isRealValidResume(file);
        if (!valid) {
            String fileExtension = fileCheckService.getFileExtension(originalFilename);
            log.error("不支持格式: {}, 请上传正确的简历文件", fileExtension);
            throw new BusinessException(UPLOAD_FAIL, "请上传正确的简历文件");
        }
        log.debug("收到简历, 文件名为: {}, 大小为: {}", originalFilename, size);


        String fileHash = FileHashUtils.calculateHash(file);
        Optional<Resume> existingResume = resumeManageService.checkIfDuplicate(fileHash);
        if (existingResume.isPresent()) {
            log.warn("检测到重复简历, resumeId={}", existingResume.get().getId());
            throw new BusinessException(UPLOAD_FAIL, "该简历已上传过，请勿重复上传");
        }

        String content = documentParseService.parseContent(file);
        if (content == null || content.isEmpty()) {
            log.warn("简历解析失败, 文件名为: {}", originalFilename);
            throw new BusinessException(BAD_REQUEST, "简历解析失败");
        }
        log.debug("简历解析成功, 文件名为: {}, 内容长度: {} 字符", originalFilename, content.length());

        // 保存简历
        Long resumeId = resumeManageService.saveResume(
                new ResumeSaveDTO(null, null, content,
                        fileHash, originalFilename, size, contentType)
        );

        String fileKey = resumeFileService.uploadResume(file);
        String fileUrl = resumeFileService.getResumeUrl(fileKey);
        resumeManageService.updateResumeStorge(resumeId, fileKey, fileUrl);
        log.debug("简历上传成功, 文件名为: {}, 文件路径为: {}", originalFilename, fileUrl);


        eventPublisher.publishEvent(new ResumeListenerDTO(resumeId, content));
        log.debug("简历事件发布成功, 文件名为: {}", originalFilename);
        return new TaskSubmitResponse(resumeId, TaskResourceType.RESUME.getValue(), TaskStatus.PENDING);
    }
}
