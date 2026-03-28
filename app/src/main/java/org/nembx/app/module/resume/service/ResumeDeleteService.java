package org.nembx.app.module.resume.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeDeleteService {
    private final ResumeManageService resumeManageService;

    private final ResumeFileService resumeFileService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long resumeId) {
        Resume resume = resumeManageService.getOneById(resumeId);

        resumeManageService.deleteResume(resumeId);
        log.info("数据库删除简历成功, id为: {}", resumeId);

        resumeFileService.deleteResume(resume.getStorageKey());
        log.info("文件系统删除简历成功, id为: {}", resumeId);
    }
}
