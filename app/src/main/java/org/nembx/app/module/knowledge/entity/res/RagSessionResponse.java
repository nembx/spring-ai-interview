package org.nembx.app.module.knowledge.entity.res;


import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */
public record RagSessionResponse(
        Long id,
        String title,
        List<Long> knowledgeBaseIds,
        LocalDateTime createdAt) {
}
