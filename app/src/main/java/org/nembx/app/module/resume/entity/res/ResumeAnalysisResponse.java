package org.nembx.app.module.resume.entity.res;


import java.util.List;

/**
 * @author Lian
 */
public record ResumeAnalysisResponse(
        // 总分 (0-100)
        Integer overallScore,

        // 各维度评分
        ScoreDetail scoreDetail,

        // 简历摘要
        String summary,

        // 优点列表
        List<String> strengths,

        // 改进建议列表
        List<Suggestion> suggestions,

        // 原始简历文本
        String originalText
) {

    /**
     * 各维度评分详情
     */
    public record ScoreDetail(
            Integer contentScore,       // 内容完整性 (0-25)
            Integer structureScore,     // 结构清晰度 (0-20)
            Integer skillMatchScore,    // 技能匹配度 (0-25)
            Integer expressionScore,    // 表达专业性 (0-15)
            Integer projectScore        // 项目经验 (0-15)
    ) {
    }

    /**
     * 改进建议
     */
    public record Suggestion(
            String category,        // 建议类别：内容、格式、技能、项目等
            String priority,        // 优先级：高、中、低
            String issue,           // 问题描述
            String recommendation   // 具体建议
    ) {
    }
}
