package org.nembx.app.module.resume.service.resume;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.FileType;
import org.nembx.app.common.service.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeFileService {
    private final FileStorage fileStorage;

    public String uploadResume(MultipartFile file){
        return fileStorage.upload(file, FileType.RESUME.getValue());
    }

    public void deleteResume(String fileKey){
        fileStorage.delete(fileKey);
    }

    public String getResumeUrl(String fileKey){
        return fileStorage.getUrl(fileKey);
    }
}
