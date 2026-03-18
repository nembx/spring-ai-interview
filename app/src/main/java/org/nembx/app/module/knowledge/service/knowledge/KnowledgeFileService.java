package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.service.RustFsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static org.nembx.app.common.enums.FileType.KNOWLEDGE;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeFileService {
    private final RustFsService rustFsService;

    public String uploadFile(MultipartFile file) {
        log.info("开始上传文件, 文件名: {}, 文件大小: {}", file.getOriginalFilename(), file.getSize());
        return rustFsService.uploadFile(file, KNOWLEDGE.getValue());
    }

    public void deleteFile(String fileKey) {
        log.info("开始删除文件, 文件名: {}", fileKey);
        rustFsService.deleteFile(fileKey);
    }

    public String getFileUrl(String fileKey) {
        log.info("开始获取文件URL, 文件名: {}", fileKey);
        return rustFsService.getFileUrl(fileKey);
    }
}
