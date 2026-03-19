package org.nembx.app.module.resume.entity.res;


import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record ResumeResponse(
        Long id,
        String fileName,
        Long fileSize,
        String contentType,
        String resumeText,
        LocalDateTime uploadTime
) {
}
