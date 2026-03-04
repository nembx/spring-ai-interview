package org.nembx.app.module.resume.enity.record.dto;


import java.util.List;

/**
 * @author Lian
 */
public record ResumeAnalysisResponseDTO(
        Integer overallScore,
        ScoreDetailDTO scoreDetail,
        String summary,
        List<String> strengths,
        List<SuggestionDTO> suggestions
) {
    public record ScoreDetailDTO(
            Integer contentScore,
            Integer structureScore,
            Integer skillMatchScore,
            Integer expressionScore,
            Integer projectScore
    ) {}

    public record SuggestionDTO(
            String category,
            String priority,
            String issue,
            String recommendation
    ) {}

}