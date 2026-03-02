package org.nembx.app.module.resume.service;


import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.status.TaskStatus;
import org.nembx.app.module.resume.enity.Resume;
import org.nembx.app.module.resume.enity.record.ResumeSaveDTO;
import org.nembx.app.module.resume.repository.ResumeRepository;
import org.nembx.app.module.resume.utils.FileHashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeManageService {
    private final ResumeRepository resumeRepository;

    @Transactional
    public Long saveResume(ResumeSaveDTO resumeSaveDTO){
        Resume resume = new Resume();
        BeanUtil.copyProperties(resumeSaveDTO, resume);
        resumeRepository.save(resume);
        log.info("简历持久化到数据库成功, 文件名为: {}", resumeSaveDTO.fileName());
        return resume.getId();
    }

    @Transactional
    public void deleteResume(Long resumeId){
        resumeRepository.deleteById(resumeId);
        log.info("简历从数据库删除成功, id为: {}", resumeId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateResume(Long resumeId, TaskStatus taskStatus){
        Integer row = resumeRepository.updateResumeTaskStatus(resumeId, taskStatus);
        if (row > 0)
            log.info("简历更新成功, id为: {}, 状态为: {}", resumeId, taskStatus);
        else
            log.warn("简历更新失败, id为: {}", resumeId);
    }

    public Optional<Resume> checkIfDuplicate(MultipartFile file){
        String fileHash = FileHashUtils.calculateHash(file);
        Optional<Resume> existing = resumeRepository.findByFileHash(fileHash);
        if (existing.isPresent()){
            log.warn("收到重复简历, 文件Hash为: {}", fileHash);
            return existing;
        }
        log.info("收到新简历, 文件Hash为: {}", fileHash);
        return Optional.empty();
    }
}
