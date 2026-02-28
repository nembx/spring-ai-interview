package org.nembx.app.module.resume.enity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
@Entity
@Table(name = "resume_analysis")
@Data
@Accessors(chain = true)
public class ResumeAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的简历ID
    private Long resumeId;

    // 总分 (0-100)
    private Integer overallScore;

    // 各维度评分
    private Integer contentScore;      // 内容完整性 (0-25)
    private Integer structureScore;    // 结构清晰度 (0-20)
    private Integer skillMatchScore;   // 技能匹配度 (0-25)
    private Integer expressionScore;   // 表达专业性 (0-15)
    private Integer projectScore;      // 项目经验 (0-15)

    // 简历摘要
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 优点列表 (JSON格式)
    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    // 改进建议列表 (JSON格式)
    @Column(columnDefinition = "TEXT")
    private String suggestionsJson;

    // 评测时间
    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}
