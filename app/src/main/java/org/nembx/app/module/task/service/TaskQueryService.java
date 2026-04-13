package org.nembx.app.module.task.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lian
 */

@Service
@RequiredArgsConstructor
public class TaskQueryService {
    private final List<TaskProvider> providers;

    private final Map<TaskResourceType, TaskProvider> taskMap = new EnumMap<>(TaskResourceType.class);

    @PostConstruct
    private void init() {
        for (TaskProvider provider : providers) {
            taskMap.put(provider.supportType(), provider);
        }
    }

    public TaskStatusResponse getTaskStatus(String resourceType, Long resourceId) {
        TaskResourceType type = TaskResourceType.fromValue(resourceType);
        TaskStatusResponse taskStatus = taskMap.get(type).getTaskStatus(resourceId);
        if (taskStatus == null)
            throw new BusinessException(ErrorCode.NOT_FOUND, "不存在该任务状态");
        return taskStatus;
    }
}
