package org.nembx.app.module.resume.service.resume;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.entity.pojo.ResumeAnalysis;
import org.nembx.app.module.resume.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeDeleteService {
    private final ResumeManageService resumeManageService;

    private final ResumeFileService resumeFileService;

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long resumeId) {
        try {
            Resume resume = resumeManageService.getOneById(resumeId);
            deleteResumeAnalyses(resumeId);

            resumeManageService.deleteResume(resumeId);
            log.info("数据库删除简历成功, id为: {}", resumeId);

            resumeFileService.deleteResume(resume.getStorageKey());
            log.info("文件系统删除简历成功, id为: {}", resumeId);
        } catch (BusinessException e) {
            log.error("删除简历失败, resumeId: {}", resumeId, e);
            throw e;
        } catch (Exception e) {
            log.error("删除简历失败, resumeId: {}", resumeId, e);
            throw new BusinessException(ErrorCode.DELETE_FAIL, "删除简历失败");
        }
    }

    private void deleteResumeAnalyses(Long resumeId) {
        List<ResumeAnalysis> analyses = resumeAnalysisRepository.findAllByResumeId(resumeId);
        if (CollectionUtil.isEmpty(analyses)) {
            return;
        }
        resumeAnalysisRepository.deleteAll(analyses);
        log.info("数据库删除简历分析结果成功, resumeId: {}, count: {}", resumeId, analyses.size());
    }
}
