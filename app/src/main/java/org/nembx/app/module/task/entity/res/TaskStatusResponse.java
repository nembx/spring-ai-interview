package org.nembx.app.module.task.entity.res;


import org.nembx.app.common.enums.TaskStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
public record TaskStatusResponse(
        Long resourceId,
        String resourceType,
        String fileName,
        TaskStatus taskStatus,
        LocalDateTime uploadTime
) {
}
