package org.nembx.app.module.task.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.task.entity.res.TaskStatusResponse;
import org.nembx.app.module.task.service.TaskQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lian
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
@Tag(name = "任务中心", description = "任务状态查询接口")
public class TaskController {
    private final TaskQueryService taskQueryService;

    @Operation(summary = "查询任务状态")
    @GetMapping("/status/{resourceType}/{resourceId}")
    public Result<TaskStatusResponse> getTaskStatus(@PathVariable String resourceType,
                                                    @PathVariable Long resourceId) {
        return Result.success(taskQueryService.getTaskStatus(resourceType, resourceId));
    }
}
