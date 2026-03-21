package org.nembx.app.module.task.entity.res;


import org.nembx.app.common.enums.TaskStatus;

/**
 * @author Lian
 */
public record TaskSubmitResponse(
        Long resourceId,
        String resourceType,
        TaskStatus taskStatus
) {
}
