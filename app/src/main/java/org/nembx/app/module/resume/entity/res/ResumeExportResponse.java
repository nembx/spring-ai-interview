package org.nembx.app.module.resume.entity.res;


/**
 * @author Lian
 */
public record ResumeExportResponse(
        byte[] pdf,
        String fileName
) {
}
