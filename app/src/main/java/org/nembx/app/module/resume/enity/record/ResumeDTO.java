package org.nembx.app.module.resume.enity.record;


/**
 * @author Lian
 */
public record ResumeDTO(
        // 简历ID
        Long resumeId,
        // 简历内容
        String resumeText
) {
}
