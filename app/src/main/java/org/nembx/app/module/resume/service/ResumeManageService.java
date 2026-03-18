package org.nembx.app.module.resume.service;


import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.TaskStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.module.resume.entity.Resume;
import org.nembx.app.module.resume.entity.ResumeAnalysis;
import org.nembx.app.module.resume.entity.dto.ResumeSaveDTO;
import org.nembx.app.module.resume.entity.res.ExportParamResponse;
import org.nembx.app.module.resume.entity.res.ResumeDetailResponse;
import org.nembx.app.module.resume.entity.res.ResumeResponse;
import org.nembx.app.module.resume.repository.ResumeAnalysisRepository;
import org.nembx.app.module.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.nembx.app.common.exception.ErrorCode.NOT_FOUND;
import static org.nembx.app.common.exception.ErrorCode.PARAM_ERROR;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeManageService {
    private final ResumeRepository resumeRepository;

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Transactional
    public Long saveResume(ResumeSaveDTO resumeSaveDTO) {
        Resume resume = new Resume();
        BeanUtil.copyProperties(resumeSaveDTO, resume);
        resumeRepository.save(resume);
        log.info("简历持久化到数据库成功, 文件名为: {}", resumeSaveDTO.fileName());
        return resume.getId();
    }

    @Transactional
    public void deleteResume(Long resumeId) {
        resumeRepository.deleteById(resumeId);
        log.info("简历从数据库删除成功, id为: {}", resumeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateResumeStatus(Long resumeId, TaskStatus taskStatus) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(
                () -> new BusinessException(NOT_FOUND, "简历不存在"));
        resume.setStatus(taskStatus);
        log.info("简历更新成功, id为: {}, 状态为: {}", resumeId, taskStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateResumeStorge(Long resumeId, String fileKey, String fileUrl) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(
                () -> new BusinessException(NOT_FOUND, "简历不存在"));
        resume.setStorageKey(fileKey);
        resume.setStorageUrl(fileUrl);
        log.info("简历更新成功, id为: {}, 存储信息为: {}, {}", resumeId, fileKey, fileUrl);
    }

    public Optional<Resume> checkIfDuplicate(String fileHash) {
        Optional<Resume> existing = resumeRepository.findByFileHash(fileHash);
        if (existing.isPresent()) {
            log.warn("收到重复简历, 文件Hash为: {}", fileHash);
            return existing;
        }
        log.info("收到新简历, 文件Hash为: {}", fileHash);
        return Optional.empty();
    }

    public Resume getOneById(Long resumeId) {
        if (resumeId == null) {
            log.warn("获取简历失败, id为空");
            throw new BusinessException(PARAM_ERROR, "id为空");
        }
        return resumeRepository.findById(resumeId).orElseThrow(
                () -> new BusinessException(NOT_FOUND, "简历不存在"));
    }

    public ResumeResponse getResumeById(Long resumeId) {
        Resume resume = getOneById(resumeId);

        return new ResumeResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getFileSize(),
                resume.getContentType(),
                resume.getContent(),
                resume.getUploadTime()
        );
    }

    public ResumeAnalysis getOneDetail(Long resumeId) {
        if (resumeId == null) {
            log.warn("获取简历分析结果失败, id为空");
            throw new BusinessException(PARAM_ERROR, "id为空");
        }
        return resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(resumeId);
    }

    public ExportParamResponse getResumeDetail(Long resumeId) {
        Resume resume = getOneById(resumeId);
        ResumeAnalysis resumeAnalysis = getOneDetail(resumeId);
        if (resumeAnalysis == null) {
            log.warn("获取简历分析结果失败, id为: {}", resumeId);
            throw new BusinessException(NOT_FOUND, "简历分析结果不存在");
        }
        ResumeDetailResponse response = new ResumeDetailResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getFileSize(),
                resume.getContentType(),
                resume.getContent(),
                resume.getUploadTime(),
                resumeAnalysis
        );
        return new ExportParamResponse(resume, response);
    }
}
