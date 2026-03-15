package org.nembx.app.module.resume.entity.dto;


/**
 * @author Lian
 */
public record ResumeListenerDTO(
        // 简历ID
        Long resumeId,
        // 简历内容
        String resumeText
) {
}
