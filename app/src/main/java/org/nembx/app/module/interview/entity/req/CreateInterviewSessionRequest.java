package org.nembx.app.module.interview.entity.req;

import java.util.List;

/**
 * @author Lian
 */
public record CreateInterviewSessionRequest(
        Long resumeId,
        String jdContent,
        String title,
        List<Long> knowledgeIds
) {
}
