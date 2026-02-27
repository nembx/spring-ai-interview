package org.nembx.app.module.resume.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.service.RustFsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeFileService {

    private final RustFsService rustFsService;

    public String uploadResume(MultipartFile file){
        return rustFsService.uploadFile(file, "resume");
    }

    public void deleteResume(String fileKey){
        rustFsService.deleteFile(fileKey);
    }

    public String getResumeUrl(String fileKey){
        return rustFsService.getFileUrl(fileKey);
    }
}
