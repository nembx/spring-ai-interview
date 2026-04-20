package org.nembx.app.module.interview.entity.res;

import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.module.interview.entity.dto.InterviewMessageDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */
public record InterviewSessionDetailResponse(
        Long sessionId,
        String jdContent,
        List<Long> knowledgeIds,
        List<InterviewMessageDTO> messages,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
