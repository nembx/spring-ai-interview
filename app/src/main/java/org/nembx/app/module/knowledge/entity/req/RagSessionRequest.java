package org.nembx.app.module.knowledge.entity.req;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author Lian
 */
public record RagSessionRequest(
        @NotNull(message = "会话ID不能为空")
        Long sessionId,
        @NotBlank(message = "问题不能为空")
        String question
) {
}
