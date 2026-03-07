package org.nembx.app.module.knowledge.enity.dto;


import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record KnowledgeListDTO(
        Long id,
        String fileName,
        String category,
        Long fileSize,
        String fileType,
        LocalDateTime uploadTime
) {
}
