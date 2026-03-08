package org.nembx.app.module.knowledge.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeDeleteService;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeManageService;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/knowledge")
@Tag(name = "知识库管理", description = "知识库管理接口")
public class KnowledgeController {

    private final KnowledgeManageService knowledgeManageService;

    private final KnowledgeDeleteService knowledgeDeleteService;

    private final KnowledgeUploadService knowledgeUploadService;

    @Operation(summary = "上传知识库")
    @PostMapping("/upload")
    public Result<String> uploadKnowledge(@RequestParam("file") MultipartFile file,
                                          @RequestParam String category) {
        knowledgeUploadService.uploadAndParse(file, category);
        return Result.success();
    }

    @Operation(summary = "根据Id获取知识库")
    @GetMapping("/get/{knowledgeId}")
    public Result<Knowledge> getKnowledgeById(@PathVariable Long knowledgeId) {
        Knowledge knowledge = knowledgeManageService.getKnowledgeById(knowledgeId);
        if (knowledge == null) {
            return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "知识库为空");
        }
        return Result.success(ErrorCode.SUCCESS.getMessage(), knowledge);
    }

    @Operation(summary = "根据Id删除知识库")
    @DeleteMapping("/delete/{knowledgeId}")
    public Result<Void> deleteKnowledge(@PathVariable Long knowledgeId) {
        knowledgeDeleteService.deleteKnowledge(knowledgeId);
        return Result.success();
    }
}
