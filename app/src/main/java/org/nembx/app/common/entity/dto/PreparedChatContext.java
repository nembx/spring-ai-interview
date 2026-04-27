package org.nembx.app.common.entity.dto;

/**
 * @author Lian
 */
public record PreparedChatContext(
        Long userMessageId,
        Long assistantMessageId
) {
}
