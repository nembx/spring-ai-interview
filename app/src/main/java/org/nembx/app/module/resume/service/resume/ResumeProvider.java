package org.nembx.app.module.resume.service.resume;

import lombok.RequiredArgsConstructor;
import org.nembx.app.common.enums.TaskResourceType;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.nembx.app.module.task.service.TaskProvider;
import org.springframework.stereotype.Service;

/**
 * @author Lian
 */
@Service
@RequiredArgsConstructor
public class ResumeProvider implements TaskProvider {
    private final ResumeManageService resumeManageService;

    @Override
    public TaskResourceType supportType() {
        return TaskResourceType.RESUME;
    }

    @Override
    public TaskStatusResponse getTaskStatus(Long resumeId) {
        Resume resume = resumeManageService.getOneById(resumeId);
        return new TaskStatusResponse(
                resume.getId(),
                TaskResourceType.RESUME.getValue(),
                resume.getFileName(),
                resume.getStatus(),
                resume.getUploadTime()
        );
    }
}
