package org.nembx.app.module.knowledge.enity.res;


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
