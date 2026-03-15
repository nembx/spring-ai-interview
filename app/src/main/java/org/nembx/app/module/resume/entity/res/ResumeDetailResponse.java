package org.nembx.app.module.resume.entity.res;


import org.nembx.app.module.resume.entity.ResumeAnalysis;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record ResumeDetailResponse(
        Long id,
        String fileName,
        Long fileSize,
        String contentType,
        String resumeText,
        LocalDateTime uploadTime,
        ResumeAnalysis analysis
) {
}
