package org.nembx.app.module.resume.enity.record.res;


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
        String fileUrl,
        String resumeText,
        LocalDateTime uploadTime,
        ResumeAnalysis analysis
) {
}
