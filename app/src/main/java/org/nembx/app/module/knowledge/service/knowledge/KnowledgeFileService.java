package org.nembx.app.module.knowledge.service.knowledge;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.service.FileStorage;
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
    private final FileStorage fileStorage;

    public String uploadFile(MultipartFile file) {
        log.info("开始上传文件, 文件名: {}, 文件大小: {}", file.getOriginalFilename(), file.getSize());
        return fileStorage.upload(file, KNOWLEDGE.getValue());
    }

    public void deleteFile(String fileKey) {
        log.info("开始删除文件, 文件名: {}", fileKey);
        fileStorage.delete(fileKey);
    }

    public String getFileUrl(String fileKey) {
        log.info("开始获取文件URL, 文件名: {}", fileKey);
        return fileStorage.getUrl(fileKey);
    }
}
