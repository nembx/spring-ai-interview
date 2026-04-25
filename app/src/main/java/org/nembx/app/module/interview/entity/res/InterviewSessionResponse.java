package org.nembx.app.module.interview.entity.res;

import org.nembx.app.common.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */
public record InterviewSessionResponse(
        Long id,
        String title,
        String jdContent,
        List<Long> knowledgeIds,
        String selectedSkill,
        SessionStatus status,
        LocalDateTime createdAt
) {
}
