package org.nembx.app.module.resume.enity.dto;


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
