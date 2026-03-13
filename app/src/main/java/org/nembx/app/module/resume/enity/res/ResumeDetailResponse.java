package org.nembx.app.module.resume.enity.res;


import org.nembx.app.module.resume.enity.ResumeAnalysis;

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
