package org.nembx.app.module.knowledge.entity.res;


import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record KnowledgeResponse(
        Long id,
        String fileName,
        String category,
        Long fileSize,
        String fileType,
        LocalDateTime uploadTime
) {
}
