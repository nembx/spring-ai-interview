package org.nembx.app.module.resume.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.service.DocumentParseService;
import org.nembx.app.common.service.FileCheckService;
import org.nembx.app.module.resume.enity.Resume;
import org.nembx.app.module.resume.enity.record.ResumeDTO;
import org.nembx.app.module.resume.enity.record.ResumeSaveDTO;
import org.nembx.app.module.resume.repository.ResumeAnalysisRepository;
import org.nembx.app.module.resume.utils.FileHashUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

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

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackOn = Exception.class)
    public void uploadAndAnalyze(MultipartFile file){
        String contentType = file.getContentType();
        Long size = file.getSize();
        String originalFilename = file.getOriginalFilename();
        // 检查文件
        boolean valid = fileCheckService.isRealValidResume(file);
        if (!valid){
            String fileExtension = fileCheckService.getFileExtension(originalFilename);
            log.error("不支持格式: {}, 请上传正确的简历文件", fileExtension);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传正确的简历文件");
        }
        log.info("收到简历, 文件名为: {}, 大小为: {}", originalFilename, size);

        Optional<Resume> existingResume = resumeManageService.checkIfDuplicate(file);
        if (existingResume.isPresent()) {
            String aiRes = handleDuplicateResume(existingResume.get());
            log.info("重复简历的分析结果为: {}", aiRes);
        }

        String content = documentParseService.parseContent(file);
        if (content == null || content.isEmpty()){
            log.warn("简历解析失败, 文件名为: {}", originalFilename);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "简历解析失败");
        }
        log.info("简历解析成功, 文件名为: {}, 内容为: {}", originalFilename, content);

        String fileKey = resumeFileService.uploadResume(file);
        String fileUrl = resumeFileService.getResumeUrl(fileKey);
        log.info("简历上传成功, 文件名为: {}, 文件路径为: {}", originalFilename, fileUrl);
        // 保存简历
        Long resumeId = resumeManageService.saveResume(
                new ResumeSaveDTO(
                        fileKey,
                        fileUrl,
                        content,
                        FileHashUtils.calculateHash(file),
                        originalFilename,
                        size,
                        contentType
                )
        );

        eventPublisher.publishEvent(new ResumeDTO(resumeId, content));
        log.info("简历事件发布成功, 文件名为: {}", originalFilename);
    }


    private String handleDuplicateResume(Resume resume){
        log.info("检测到重复简历，返回历史分析结果: resumeId={}", resume.getId());
        // TODO: 获取历史分析结果
        return resumeAnalysisRepository.findAnalysisByResumeId(resume.getId()).toString();
    }
}
