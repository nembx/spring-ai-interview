package org.nembx.app.module.knowledge.service.knowledge;

import lombok.RequiredArgsConstructor;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.nembx.app.module.task.service.TaskProvider;
import org.springframework.stereotype.Service;

/**
 * @author Lian
 */

@RequiredArgsConstructor
@Service
public class KnowledgeProvider implements TaskProvider {
    private final KnowledgeManageService knowledgeManageService;

    @Override
    public TaskResourceType supportType() {
        return TaskResourceType.KNOWLEDGE;
    }

    @Override
    public TaskStatusResponse getTaskStatus(Long knowledgeId) {
        Knowledge knowledge = knowledgeManageService.getOneById(knowledgeId);
        return new TaskStatusResponse(
                knowledge.getId(),
                TaskResourceType.KNOWLEDGE.getValue(),
                knowledge.getFileName(),
                knowledge.getTaskStatus(),
                knowledge.getUploadTime()
        );
    }
}
