package org.nembx.app.module.resume.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.resume.entity.res.JdMatchResponse;
import org.nembx.app.module.resume.service.JdMatchService;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lian
 */

@RequiredArgsConstructor
@RequestMapping("/jd-match")
@RestController
@Tag(name = "职位匹配", description = "职位匹配相关接口")
public class JdMatchController {
    private final JdMatchService jdMatchService;

    @PostMapping("/match/{resumeId}")
    @Operation(summary = "职位匹配分析")
    public Result<Void> match(@PathVariable Long resumeId, @RequestParam String jdContent) {
        jdMatchService.match(resumeId, jdContent);
        return Result.success();
    }

    @GetMapping("/result/{resumeId}")
    @Operation(summary = "获取职位匹配结果")
    public Result<JdMatchResponse> getResult(@PathVariable Long resumeId) {
        return Result.success(jdMatchService.getJdMatchResult(resumeId));
    }
}
