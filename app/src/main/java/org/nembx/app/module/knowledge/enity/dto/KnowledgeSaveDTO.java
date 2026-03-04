package org.nembx.app.module.knowledge.enity.dto;


import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record KnowledgeSaveDTO(
        String fileHash,
        String fileName,
        String category,
        Long fileSize,
        String fileType,
        String storageKey,
        String storageUrl,
        LocalDateTime uploadTime
) {
}
