package org.nembx.app.module.knowledge.controller;


import lombok.RequiredArgsConstructor;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.service.KnowledgeManageService;
import org.nembx.app.module.knowledge.service.KnowledgeUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeManageService knowledgeManageService;

    private final KnowledgeUploadService knowledgeUploadService;

    @PostMapping("/upload")
    public Result<String> uploadKnowledge(@RequestParam("file") MultipartFile file,
                                          @RequestParam String category) {
        knowledgeUploadService.uploadAndParse(file, category);
        return Result.success();
    }

    @GetMapping("/get/{knowledgeId}")
    public Result<Knowledge> getKnowledgeById(@PathVariable Long knowledgeId) {
        Knowledge knowledge = knowledgeManageService.getKnowledgeById(knowledgeId);
        if (knowledge == null) {
            return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "知识库为空");
        }
        return Result.success(ErrorCode.SUCCESS.getMessage(), knowledge);
    }

    @DeleteMapping("/delete/{knowledgeId}")
    public Result<Void> deleteKnowledge(@PathVariable Long knowledgeId) {
        knowledgeManageService.deleteKnowledge(knowledgeId);
        return Result.success();
    }
}
