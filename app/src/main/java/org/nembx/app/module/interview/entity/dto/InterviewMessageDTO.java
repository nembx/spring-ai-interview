package org.nembx.app.module.interview.entity.dto;

import org.nembx.app.common.enums.MessageType;

/**
 * @author Lian
 */
public record InterviewMessageDTO(
        Long id,
        MessageType type,
        String content
) {
}
