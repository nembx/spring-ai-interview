package org.nembx.app.module.task.service;


import lombok.RequiredArgsConstructor;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeManageService;
import org.nembx.app.module.resume.service.ResumeManageService;
import org.nembx.app.module.task.entity.TaskResourceType;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.springframework.stereotype.Service;

/**
 * @author Lian
 */
@Service
@RequiredArgsConstructor
public class TaskQueryService {
    private final ResumeManageService resumeManageService;

    private final KnowledgeManageService knowledgeManageService;

    public TaskStatusResponse getTaskStatus(String resourceType, Long resourceId) {
        TaskResourceType type = TaskResourceType.fromValue(resourceType);
        return switch (type) {
            case RESUME -> resumeManageService.getTaskStatus(resourceId);
            case KNOWLEDGE -> knowledgeManageService.getTaskStatus(resourceId);
        };
    }
}
