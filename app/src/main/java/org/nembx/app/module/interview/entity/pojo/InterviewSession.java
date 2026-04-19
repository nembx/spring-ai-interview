package org.nembx.app.module.interview.entity.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SoftDelete;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lian
 */

@Entity
@Table(name = "interview_session")
@Data
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@EqualsAndHashCode(exclude = "knowledges")
@ToString(exclude = "knowledges")
@Schema(description = "面试会话")
public class InterviewSession {
    @Id
    @Schema(description = "面试会话ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "对应简历ID")
    private Long resumeId;

    @Schema(description = "面试会话标题")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "职位描述")
    private String jdContent;

    @Enumerated(EnumType.STRING)
    @Schema(description = "会话状态")
    private SessionStatus status = SessionStatus.ACTIVE;

    @Schema(description = "已提问数")
    private Integer questionCount = 0;

    @Schema(description = "最大题数")
    private Integer maxQuestions = 8;

    @Schema(description = "关联的知识库")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "interview_session_knowledge",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "knowledge_id")
    )
    @JsonIgnore
    private List<Knowledge> knowledges = new ArrayList<>();

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后一次消息更新时间")
    private LocalDateTime updatedAt;

    @Transient
    @Schema(description = "关联的知识库ID列表")
    public List<Long> getKnowledgeIds() {
        if (knowledges == null) {
            return List.of();
        }
        return knowledges.stream().map(Knowledge::getId).toList();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
