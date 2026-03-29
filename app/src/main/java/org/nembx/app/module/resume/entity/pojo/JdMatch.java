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

@Table(name = "jd_match")
@Data
@Entity
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@Schema(description = "职位匹配结果")
public class JdMatch {
    @Id
    @Schema(description = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "职位描述")
    private String jdContent;

    @Schema(description = "总分")
    private Integer overallScore;

    @Schema(description = "jd匹配度")
    private Integer matchScore;

    @Schema(description = "缺少技能列表")
    @Column(columnDefinition = "TEXT")
    private String missingSkillsJson;

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
