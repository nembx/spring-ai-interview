package org.nembx.app.module.resume.entity.pojo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
@Entity
@Table(name = "resume_analysis")
@Data
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@Schema(description = "简历分析结果")
public class ResumeAnalysis {
    @Id
    @Schema(description = "分析ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "总分")
    private Integer overallScore;

    // 内容完整性 (0-25)
    @Schema(description = "内容完整性")
    private Integer contentScore;

    // 结构清晰度 (0-20)
    @Schema(description = "结构清晰度")
    private Integer structureScore;

    // 技能匹配度 (0-25)
    @Schema(description = "技能匹配度")
    private Integer skillMatchScore;

    // 表达专业性 (0-15)
    @Schema(description = "表达专业性")
    private Integer expressionScore;

    // 项目经验 (0-15)
    @Schema(description = "项目经验")
    private Integer projectScore;

    @Schema(description = "简历摘要")
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Schema(description = "优点列表")
    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Schema(description = "改进建议列表")
    @Column(columnDefinition = "TEXT")
    private String suggestionsJson;

    @Schema(description = "分析时间")
    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}
