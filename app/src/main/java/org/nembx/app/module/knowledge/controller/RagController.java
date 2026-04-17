package org.nembx.app.module.knowledge.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.knowledge.entity.req.CreateRagSessionRequest;
import org.nembx.app.module.knowledge.entity.req.RagSessionRequest;
import org.nembx.app.module.knowledge.entity.res.RagSessionDetailResponse;
import org.nembx.app.module.knowledge.entity.res.RagSessionResponse;
import org.nembx.app.module.knowledge.service.rag.RagChatService;
import org.nembx.app.module.knowledge.service.rag.RagManageService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Lian
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/rag")
@Tag(name = "rag会话管理", description = "rag会话管理接口")
public class RagController {
    private final RagChatService ragChatService;

    private final RagManageService ragManageService;

    @Operation(summary = "创建rag会话")
    @PostMapping("/create")
    public Result<RagSessionResponse> createRagSession(@Valid @RequestBody CreateRagSessionRequest request) {
        return Result.success(ragManageService.createSession(request));
    }

    @Operation(summary = "获取所有rag会话")
    @GetMapping("/sessions")
    public Result<List<RagSessionResponse>> getSessionsByStatus(@RequestParam SessionStatus status) {
        return Result.success(ragManageService.getSessionsByStatus(status));
    }

    @Operation(summary = "根据Id删除rag会话")
    @DeleteMapping("/delete/{sessionId}")
    public Result<Void> deleteRagSession(@PathVariable Long sessionId) {
        ragManageService.deleteSession(sessionId);
        return Result.success();
    }

    @Operation(summary = "根据Id获取rag会话详情")
    @GetMapping("/detail/{sessionId}")
    public Result<RagSessionDetailResponse> getSessionDetail(@PathVariable Long sessionId) {
        return Result.success(ragManageService.getSessionDetail(sessionId));
    }

    @Operation(summary = "更新rag会话知识")
    @PutMapping("/updateSessionKnowledge/{sessionId}")
    public Result<Void> updateKnowledge(@PathVariable Long sessionId, @RequestBody List<Long> knowledgeIds) {
        ragManageService.updateSessionKnowledge(sessionId, knowledgeIds);
        return Result.success();
    }

    @Operation(summary = "进行rag对话")
    @PostMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody RagSessionRequest request) {
        return ragChatService.chat(request.sessionId(), request.question());
    }

    @Operation(summary = "更新rag会话标题")
    @PutMapping("/updateTitle/{sessionId}")
    public Result<Void> updateTitle(@PathVariable Long sessionId, @RequestBody String title) {
        ragManageService.updateSessionTitle(sessionId, title);
        return Result.success();
    }

    @Operation(summary = "更新rag会话状态")
    @PutMapping("/updateStatus/{sessionId}")
    public Result<Void> updateSessionStatus(@PathVariable Long sessionId,
                                            @RequestParam SessionStatus status) {
        ragManageService.updateSessionStatus(sessionId, status);
        return Result.success();
    }
}
