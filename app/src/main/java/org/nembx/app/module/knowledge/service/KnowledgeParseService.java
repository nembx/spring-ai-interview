package org.nembx.app.module.knowledge.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.service.DocumentParseService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeParseService {
    private final DocumentParseService documentParseService;

    public String parseContent(MultipartFile file) {
        log.info("开始解析文件: {}", file.getOriginalFilename());
        return documentParseService.parseContent(file);
    }
}
