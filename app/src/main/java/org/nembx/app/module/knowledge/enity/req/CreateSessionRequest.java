package org.nembx.app.module.knowledge.enity.req;


import java.util.List;

/**
 * @author Lian
 */
public record CreateSessionRequest(
        List<Long> knowledgeIds,
        String title
) {
}
