package org.nembx.app.module.resume.entity.dto;


/**
 * @author Lian
 */
public record ResumeSaveDTO(
        String storageKey,
        String storageUrl,
        String content,
        String fileHash,
        String fileName,
        Long fileSize,
        String contentType
) {
}
