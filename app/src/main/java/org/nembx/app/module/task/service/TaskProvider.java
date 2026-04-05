package org.nembx.app.module.task.service;

import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;

/**
 * @author Lian
 */
public interface TaskProvider {
    TaskResourceType supportType();
    TaskStatusResponse getTaskStatus(Long id);
}
