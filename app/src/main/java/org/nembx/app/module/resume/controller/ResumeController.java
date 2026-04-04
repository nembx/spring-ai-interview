package org.nembx.app.module.resume.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.entity.res.ExportParamResponse;
import org.nembx.app.module.resume.entity.res.ResumeDetailResponse;
import org.nembx.app.module.resume.entity.res.ResumeExportResponse;
import org.nembx.app.module.resume.entity.res.ResumeResponse;
import org.nembx.app.module.resume.service.resume.*;
import org.nembx.app.module.task.entity.res.TaskSubmitResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Lian
 */

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "简历管理", description = "简历管理接口")
public class ResumeController {
    private final ResumeUploadService resumeUploadService;

    private final ResumeManageService resumeManageService;

    private final ResumeDeleteService resumeDeleteService;

    private final ResumeExportService resumeExportService;

    private final ResumeAiService resumeAiService;

    @Operation(summary = "上传简历")
    @PostMapping("/upload")
    public Result<TaskSubmitResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        return Result.success(resumeUploadService.uploadAndAnalyze(file));
    }

    @Operation(summary = "获取简历")
    @GetMapping("/get/{resumeId}")
    public Result<ResumeResponse> getResume(@PathVariable Long resumeId) {
        return Result.success(resumeManageService.getResumeById(resumeId));
    }

    @Operation(summary = "获取简历详情")
    @GetMapping("/detail/{resumeId}")
    public Result<ResumeDetailResponse> getResumeDetail(@PathVariable Long resumeId) {
        ExportParamResponse exportParamResponse = resumeManageService.getResumeDetail(resumeId);
        return Result.success(exportParamResponse.resumeDetailResponse());
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/delete/{resumeId}")
    public Result<Void> deleteResume(@PathVariable Long resumeId) {
        resumeDeleteService.deleteResume(resumeId);
        return Result.success();
    }

    @Operation(summary = "导出简历")
    @GetMapping("/export/{resumeId}")
    public ResponseEntity<byte[]> exportResume(@PathVariable Long resumeId) {
        try {
            ResumeExportResponse response = resumeExportService.exportResumeAnalysis(resumeId);
            String filename = URLEncoder.encode(response.fileName(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(response.pdf());
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("导出简历失败, resumeId={}", resumeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "重新分析简历")
    @PostMapping("/reAnalyze/{resumeId}")
    public Result<Void> reAnalyzeResume(@PathVariable Long resumeId) {
        Resume resume = resumeManageService.getOneById(resumeId);
        resumeAiService.analyzeResume(resumeId, resume.getContent());
        return Result.success();
    }
}
