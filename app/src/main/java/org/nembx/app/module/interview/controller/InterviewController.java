package org.nembx.app.module.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.interview.entity.req.CreateInterviewSessionRequest;
import org.nembx.app.module.interview.entity.req.InterviewChatRequest;
import org.nembx.app.module.interview.entity.res.InterviewSessionDetailResponse;
import org.nembx.app.module.interview.entity.res.InterviewSessionResponse;
import org.nembx.app.module.interview.service.InterviewChatService;
import org.nembx.app.module.interview.service.InterviewManageService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Lian
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/interview")
@Tag(name = "面试会话管理", description = "面试会话管理接口")
public class InterviewController {
    private final InterviewManageService interviewManageService;

    private final InterviewChatService interviewChatService;

    @Operation(summary = "创建interview会话")
    @PostMapping("/create")
    public Result<InterviewSessionResponse> createInterviewSession(@RequestBody CreateInterviewSessionRequest req) {
        return Result.success(interviewManageService.createInterviewSession(req));
    }

    @Operation(summary = "获取所有interview会话")
    @GetMapping("/sessions")
    public Result<List<InterviewSessionResponse>> getSessionsByStatus(@RequestParam SessionStatus status) {
        return Result.success(interviewManageService.getSessionsByStatus(status));
    }

    @Operation(summary = "根据Id获取interview会话详情")
    @GetMapping("/detail/{sessionId}")
    public Result<InterviewSessionDetailResponse> getSessionDetail(@PathVariable Long sessionId) {
        return Result.success(interviewManageService.getSessionDetail(sessionId));
    }

    @Operation(summary = "删除interview会话")
    @DeleteMapping("/delete/{sessionId}")
    public Result<Void> deleteInterviewSession(@PathVariable Long sessionId) {
        interviewManageService.deleteSession(sessionId);
        return Result.success();
    }

    @Operation(summary = "更新interview会话状态")
    @PutMapping("/updateStatus/{sessionId}")
    public Result<Void> updateSessionStatus(@PathVariable Long sessionId,
                                            @RequestParam SessionStatus status) {
        interviewManageService.updateSessionStatus(sessionId, status);
        return Result.success();
    }

    @Operation(summary = "更新interview会话标题")
    @PutMapping("/updateTitle/{sessionId}")
    public Result<Void> updateTitle(@PathVariable Long sessionId, @RequestBody String title) {
        interviewManageService.updateSessionTitle(sessionId, title);
        return Result.success();
    }

    @Operation(summary = "文本面试对话（SSE流式）")
    @PostMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody InterviewChatRequest request) {
        return interviewChatService.chat(request.sessionId(), request.question());
    }

    @Operation(summary = "语音面试对话（语音进，语音出）")
    @PostMapping(
            value = "/voice-chat/{sessionId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "audio/mpeg"
    )
    public byte[] voiceChat(@PathVariable Long sessionId,
                            @RequestPart("audio") MultipartFile audioFile) {
        return interviewChatService.voiceChat(sessionId, audioFile.getResource()).blockFirst();
    }
}
