package org.nembx.app.module.knowledge.entity.req;


import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * @author Lian
 */
public record CreateRagSessionRequest(
        @NotEmpty(message = "知识库ID列表不能为空")
        List<Long> knowledgeIds,
        String title
) {
}
