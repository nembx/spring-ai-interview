package org.nembx.app.common.service;


import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.nembx.app.common.enums.ContentType;
import org.nembx.app.common.enums.FileType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Lian
 */

@Service
@Slf4j
public class FileCheckService {
    // 允许的文件类型
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            ContentType.DOCX.getValue(),
            ContentType.PDF.getValue(),
            ContentType.MARKDOWN.getValue(),
            ContentType.TXT.getValue()
    );

    private final Map<FileType, Set<String>> RULES = new EnumMap<>(FileType.class);

    private final Tika tika = new Tika();

    @PostConstruct
    private void init() {
        RULES.put(FileType.RESUME, ALLOWED_CONTENT_TYPES);
        RULES.put(FileType.KNOWLEDGE, ALLOWED_CONTENT_TYPES);
    }

    public boolean isRealValid(MultipartFile file, FileType fileType) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Tika 通过魔数 (Magic Number) 探测真实的 MIME 类型
            String realContentType = tika.detect(inputStream).toLowerCase();
            String filename = file.getOriginalFilename();

            // 特殊处理：Markdown 本质是纯文本，Tika 有时会将其识别为 text/plain
            // 只要真实类型在我们的允许列表里（包含 text/plain 和 text/markdown），即可放行
            Set<String> allowedTypes = RULES.get(fileType);
            if (CollectionUtil.isEmpty(allowedTypes) || !allowedTypes.contains(realContentType)) {
                log.warn("非法的文件类型. 文件名: {}, 探测类型: {}", filename, realContentType);
                return false;
            }

            // 如果 Tika 探测出是纯文本 (比如 md 或 txt 会被探测为 text/plain)
            if (ContentType.TXT.getValue().equals(realContentType)) {
                String extension = getFileExtension(filename);
                if (!ALLOWED_CONTENT_TYPES.contains(extension)) {
                    log.warn("危险的纯文本后缀被拦截. 文件名: {}, 探测类型: {}", filename, realContentType);
                    return false; // 拦截 resume.bat, script.sh 等伪装者
                }
            }
            log.debug("文件校验通过. 文件名: {}, 真实类型: {}", filename, realContentType);
            return true;
        } catch (IOException e) {
            log.error("读取文件流失败，无法校验文件类型", e);
            return false;
        }
    }

    /**
     * 辅助方法：获取文件后缀名
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
