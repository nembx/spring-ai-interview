package org.nembx.app.module.knowledge.controller;


import lombok.RequiredArgsConstructor;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.knowledge.enity.Knowledge;
import org.nembx.app.module.knowledge.service.KnowledgeManageService;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lian
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/knowledge")
public class KnowLedgeController {

    private final KnowledgeManageService knowledgeManageService;

    @GetMapping("/get/{knowledgeId}")
    public Result<Knowledge> getKnowledgeById(@PathVariable Long knowledgeId) {
        Knowledge knowledge = knowledgeManageService.getKnowledgeById(knowledgeId);
        if (knowledge == null){
            return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "知识库为空");
        }
        return Result.success(ErrorCode.SUCCESS.getMessage(), knowledge);
    }

    @PostMapping("/delete/{knowledgeId}")
    public Result<Void> deleteKnowledge(@PathVariable Long knowledgeId) {
        knowledgeManageService.deleteKnowledge(knowledgeId);
        return Result.success();
    }
}
