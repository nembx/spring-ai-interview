package org.nembx.app.module.interview.entity.req;

import jakarta.validation.constraints.NotNull;

/**
 * @author Lian
 */
public record InterviewChatRequest(
        @NotNull Long sessionId,
        @NotNull String question
) {
}
