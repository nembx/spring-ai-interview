package org.nembx.app.common.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * @author Lian
 */

@Service
@Slf4j
public class FileCheckService {
    // 允许的文件类型
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", // .pdf
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "text/markdown",   // .md
            "text/plain"       // .txt
    );

    private final Tika tika = new Tika();

    public boolean isRealValidResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            return false;
        }

        try {
            // Tika 通过魔数 (Magic Number) 探测真实的 MIME 类型
            String realContentType = tika.detect(file.getInputStream());
            String filename = file.getOriginalFilename();

            // 特殊处理：Markdown 本质是纯文本，Tika 有时会将其识别为 text/plain
            // 只要真实类型在我们的允许列表里（包含 text/plain 和 text/markdown），即可放行
            boolean isValid = ALLOWED_CONTENT_TYPES.contains(realContentType.toLowerCase());

            if (isValid) {
                log.info("简历文件校验通过. 文件名: {}, 真实类型: {}", filename, realContentType);
                return true;
            } else {
                log.warn("非法的文件类型. 文件名: {}, 探测到的真实类型: {}", filename, realContentType);
                return false;
            }

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
