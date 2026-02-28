package org.nembx.app.module.resume.controller;


import lombok.RequiredArgsConstructor;
import org.nembx.app.common.result.Result;
import org.nembx.app.module.resume.service.ResumeUploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Lian
 */

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeUploadService resumeUploadService;
    @PostMapping("/upload")
    public Result<String> uploadResume(@RequestParam ("file") MultipartFile file) {
        resumeUploadService.uploadAndAnalyze(file);
        return Result.success();
    }
}
