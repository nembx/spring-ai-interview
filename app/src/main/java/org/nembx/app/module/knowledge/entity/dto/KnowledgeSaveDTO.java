package org.nembx.app.module.knowledge.entity.dto;


import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record KnowledgeSaveDTO(
        String fileHash,
        String fileName,
        String category,
        String content,
        Long fileSize,
        String fileType,
        String storageKey,
        String storageUrl,
        LocalDateTime uploadTime
) {
}
