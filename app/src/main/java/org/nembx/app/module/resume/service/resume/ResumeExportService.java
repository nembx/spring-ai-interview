package org.nembx.app.module.resume.service.resume;


import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.service.PdfExportService;
import org.nembx.app.common.utils.JsonUtils;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.entity.pojo.ResumeAnalysis;
import org.nembx.app.module.resume.entity.res.ExportParamResponse;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse.ScoreDetail;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse.Suggestion;
import org.nembx.app.module.resume.entity.res.ResumeDetailResponse;
import org.nembx.app.module.resume.entity.res.ResumeExportResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeExportService {
    private final PdfExportService pdfExportService;

    private final ResumeManageService resumeManageService;

    public ResumeExportResponse exportResumeAnalysis(Long resumeId) {
        ExportParamResponse exportParamResponse = resumeManageService.getResumeDetail(resumeId);
        Resume resume = exportParamResponse.resume();
        // 转换
        ResumeAnalysisResponse resumeAnalysisResponse = toResumeAnalysisResponse(exportParamResponse.resumeDetailResponse());
        log.debug("开始导出简历分析结果, 简历ID: {}, 响应: {}", resumeId, resumeAnalysisResponse);
        return new ResumeExportResponse(
                pdfExportService.exportResumeAnalysis(resume, resumeAnalysisResponse),
                toPdfFileName(resume.getFileName())
        );
    }

    private String toPdfFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "resume-analysis.pdf";
        }
        int dot = originalFileName.lastIndexOf('.');
        String base = dot > 0 ? originalFileName.substring(0, dot) : originalFileName;
        return base + "-分析报告.pdf";
    }

    private ResumeAnalysisResponse toResumeAnalysisResponse(ResumeDetailResponse resumeDetailResponse) {
        ResumeAnalysis analysis = resumeDetailResponse.analysis();
        ScoreDetail scoreDetail = new ScoreDetail(
                analysis.getContentScore(),
                analysis.getStructureScore(),
                analysis.getSkillMatchScore(),
                analysis.getExpressionScore(),
                analysis.getProjectScore()
        );

        List<String> strengths = JsonUtils.fromJson(
                analysis.getStrengthsJson(), new TypeReference<>() {
                });
        List<Suggestion> suggestions = JsonUtils.fromJson(
                analysis.getSuggestionsJson(), new TypeReference<>() {
                });

        return new ResumeAnalysisResponse(
                analysis.getOverallScore(),
                scoreDetail,
                analysis.getSummary(),
                strengths,
                suggestions,
                resumeDetailResponse.resumeText()
        );
    }
}
