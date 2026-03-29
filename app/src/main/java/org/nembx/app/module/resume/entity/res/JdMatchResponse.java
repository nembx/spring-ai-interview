package org.nembx.app.module.resume.entity.res;


import org.nembx.app.module.resume.entity.dto.JdMatchResponseDTO;

import java.util.List;

/**
 * @author Lian
 */
public record JdMatchResponse(
        String jdContent,

        // 总分 (0-100)
        Integer overallScore,

        // 职位匹配度
        Integer matchScore,

        // 缺少技能列表
        List<MissingSkills> missingSkills,

        // 改进建议列表
        List<Suggestion> suggestions
) {
    /**
     * 缺少技能
     */
    public record MissingSkills(
            String skillName,
            String skillLevel
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
