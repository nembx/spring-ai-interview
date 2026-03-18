package org.nembx.app.module.knowledge.entity.dto;


import org.nembx.app.common.enums.MessageType;

/**
 * @author Lian
 */
public record RagMessageDTO(
        Long id,
        MessageType type,
        String content
) {
}
