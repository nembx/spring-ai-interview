package org.nembx.app.module.knowledge.entity.res;


import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeListDTO;
import org.nembx.app.module.knowledge.entity.dto.RagMessageDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */
public record RagSessionDetailResponse(
        Long id,
        String title,
        List<KnowledgeListDTO> knowledgeBases,
        List<RagMessageDTO> messages,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
