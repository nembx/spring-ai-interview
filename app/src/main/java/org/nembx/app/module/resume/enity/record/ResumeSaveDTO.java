package org.nembx.app.module.resume.enity.record;


/**
 * @author Lian
 */
public record ResumeSaveDTO(
        String fileKey,
        String fileUrl,
        String content,
        String fileHash,
        String fileName,
        Long fileSize,
        String contentType
) {
}
